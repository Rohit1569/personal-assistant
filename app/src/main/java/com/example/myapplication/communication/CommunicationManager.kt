package com.example.myapplication.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.myapplication.plugin.CommunicationApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationManager @Inject constructor(private val context: Context) {

    fun sendMessage(app: CommunicationApp, recipient: String, message: String): Result<Unit> {
        return try {
            val intent = when (app) {
                CommunicationApp.WHATSAPP -> {
                    // Set flag for Accessibility Service to know this is an intentional auto-send
                    AutomationState.requestAutoSend()
                    getWhatsAppIntent(recipient, message)
                }
                CommunicationApp.GMAIL -> getGmailIntent(recipient, message)
                CommunicationApp.SLACK -> getSlackIntent(message)
                CommunicationApp.SMS -> getSmsIntent(recipient, message)
            }
            launchIntent(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Directly marks the Google Calendar using Intents.
     */
    fun scheduleOnCalendar(title: String, startTime: Long, durationMinutes: Int, location: String?): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + (durationMinutes * 60000))
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.DESCRIPTION, "Scheduled by AI Assistant")
            }
            launchIntent(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun launchIntent(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun getWhatsAppIntent(phone: String, message: String): Intent {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }
    }

    private fun getGmailIntent(email: String, message: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    private fun getSmsIntent(phone: String, message: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
            putExtra("sms_body", message)
        }
    }

    private fun getSlackIntent(message: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }
}
