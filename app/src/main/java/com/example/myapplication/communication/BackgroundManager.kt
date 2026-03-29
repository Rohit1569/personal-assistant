package com.example.myapplication.communication

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundManager @Inject constructor(private val context: Context) {

    data class CalendarInfo(val id: Long, val account: String, val displayName: String)
    data class CalendarEvent(val id: Long, val title: String, val startTime: Long)

    fun insertCalendarEventBackground(title: String, startTime: Long, durationMinutes: Int, location: String?): Result<CalendarInfo> {
        return try {
            val calInfo = getGoogleCalendarInfo() ?: return Result.failure(Exception("No writable Google Calendar found. Ensure you are signed in and sync is on."))
            
            val cr = context.contentResolver
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, startTime + (durationMinutes * 60000))
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, "Automated by AI Assistant")
                put(CalendarContract.Events.CALENDAR_ID, calInfo.id)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.EVENT_LOCATION, location ?: "Office")
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            
            val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLong() ?: -1L
            
            if (eventId == -1L) throw Exception("Calendar provider failed to insert event.")

            addReminder(eventId, 15)
            
            // CRITICAL: Force immediate sync and broadcast change
            forceCalendarSync(calInfo.account)
            
            Log.d("AI_BOT", "Success! Event ID: $eventId on ${calInfo.account}")
            Result.success(calInfo)
        } catch (e: Exception) {
            Log.e("AI_BOT", "Calendar Error: ${e.message}")
            Result.failure(e)
        }
    }

    fun queryCalendarEvents(startTime: Long, endTime: Long): Result<List<CalendarEvent>> {
        return try {
            val events = mutableListOf<CalendarEvent>()
            val projection = arrayOf(CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART)
            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.DELETED} != 1"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    events.add(CalendarEvent(it.getLong(0), it.getString(1) ?: "No Title", it.getLong(2)))
                }
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteCalendarEvent(title: String): Result<Int> {
        return try {
            val selection = "${CalendarContract.Events.TITLE} LIKE ? AND ${CalendarContract.Events.DELETED} != 1"
            val selectionArgs = arrayOf("%$title%")
            val deletedCount = context.contentResolver.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs)
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addReminder(eventId: Long, minutes: Int) {
        try {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.MINUTES, minutes)
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
        } catch (e: Exception) {
            Log.e("AI_BOT", "Reminder error: ${e.message}")
        }
    }

    private fun forceCalendarSync(accountName: String) {
        try {
            // 1. Notify the system that the provider content has changed
            context.contentResolver.notifyChange(CalendarContract.Events.CONTENT_URI, null)
            
            // 2. Explicitly request a sync for the specific Google account
            val accounts = AccountManager.get(context).accounts
            val targetAccount = accounts.find { it.name == accountName && it.type == "com.google" }
            
            if (targetAccount != null) {
                val extras = Bundle().apply {
                    putBoolean(android.content.ContentResolver.SYNC_EXTRAS_MANUAL, true)
                    putBoolean(android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                }
                android.content.ContentResolver.requestSync(targetAccount, CalendarContract.AUTHORITY, extras)
                Log.d("AI_BOT", "Immediate Sync Requested for: $accountName")
            }
            
            // 3. Broadcast intent to refresh calendar apps
            val refreshIntent = Intent(Intent.ACTION_PROVIDER_CHANGED, CalendarContract.Events.CONTENT_URI)
            context.sendBroadcast(refreshIntent)
            
        } catch (e: Exception) {
            Log.e("AI_BOT", "Sync trigger failed: ${e.message}")
        }
    }

    private fun getGoogleCalendarInfo(): CalendarInfo? {
        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.Calendars.IS_PRIMARY)
            val cursor = context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)
            var bestInfo: CalendarInfo? = null
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val accountName = it.getString(1) ?: ""
                    val accountType = it.getString(2) ?: ""
                    val isPrimary = it.getInt(3) != 0
                    if (accountType == "com.google") {
                        val info = CalendarInfo(id, accountName, accountName)
                        if (isPrimary) return info
                        if (bestInfo == null || accountName.contains("@gmail.com")) bestInfo = info
                    }
                }
            }
            bestInfo
        } catch (e: Exception) { null }
    }

    fun initiateCall(recipient: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$recipient")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) } catch (e: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$recipient")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        }
    }
}
