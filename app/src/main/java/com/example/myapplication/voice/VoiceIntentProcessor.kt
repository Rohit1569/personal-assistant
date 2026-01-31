package com.example.myapplication.voice

import com.example.myapplication.plugin.CommunicationApp
import com.example.myapplication.plugin.IntentResult
import java.util.Calendar

class VoiceIntentProcessor {

    private val appointmentKeywords = listOf(
        "nail", "haircut", "doctor", "plumber", "electrician", "specialist", 
        "client", "partner", "prospect", "real estate", "handyman", "roofer", 
        "siding", "patio", "designer", "fence", "mechanic", "dinner", "sport", 
        "coach", "grocery", "meeting", "appointment", "event"
    )

    private val monthMap = mapOf(
        "january" to Calendar.JANUARY, "february" to Calendar.FEBRUARY, "march" to Calendar.MARCH,
        "april" to Calendar.APRIL, "may" to Calendar.MAY, "june" to Calendar.JUNE,
        "july" to Calendar.JULY, "august" to Calendar.AUGUST, "september" to Calendar.SEPTEMBER,
        "october" to Calendar.OCTOBER, "november" to Calendar.NOVEMBER, "december" to Calendar.DECEMBER,
        "jan" to Calendar.JANUARY, "feb" to Calendar.FEBRUARY, "mar" to Calendar.MARCH, "apr" to Calendar.APRIL,
        "jun" to Calendar.JUNE, "jul" to Calendar.JULY, "aug" to Calendar.AUGUST, "sep" to Calendar.SEPTEMBER,
        "oct" to Calendar.OCTOBER, "nov" to Calendar.NOVEMBER, "dec" to Calendar.DECEMBER
    )

    fun parse(text: String): IntentResult {
        // Clean text: remove ordinals like 30th -> 30, 2nd -> 2
        val lowerText = text.lowercase()
            .replace(Regex("(\\d+)(st|nd|rd|th)"), "$1")
            .replace(",", "").replace(".", "").trim()
        
        return when {
            lowerText.startsWith("call") -> parseCallIntent(lowerText)

            lowerText.contains("last message") || lowerText.contains("check message") -> {
                val app = if (lowerText.contains("mail") || lowerText.contains("gmail")) CommunicationApp.GMAIL else CommunicationApp.WHATSAPP
                val contactName = lowerText.substringAfter("from ").substringBefore(" in").substringBefore(" on").trim()
                IntentResult.LastMessageQuery(app, contactName)
            }

            lowerText.contains("cancel all") || lowerText.contains("clear all") -> parseCalendarRangeDelete(lowerText)

            lowerText.startsWith("cancel") || lowerText.startsWith("delete") -> {
                val title = lowerText.substringAfter("cancel ").substringAfter("delete ").replace("meeting", "").trim()
                IntentResult.CalendarDelete(title)
            }

            lowerText.contains("any meeting") || lowerText.contains("what is scheduled") || lowerText.contains("check my calendar") -> parseCalendarQuery(lowerText)

            lowerText.contains("uber") || lowerText.contains("ola") -> {
                val provider = if (lowerText.contains("ola")) "OLA" else "UBER"
                var dest = lowerText.substringAfter("to ").trim()
                if (dest.contains("railway station")) dest = "Pune Station"
                IntentResult.BookCab(provider, dest)
            }

            lowerText.contains("send") || lowerText.contains("mail") || lowerText.contains("whatsapp") -> {
                val app = when {
                    lowerText.contains("mail") || lowerText.contains("gmail") -> CommunicationApp.GMAIL
                    lowerText.contains("whatsapp") -> CommunicationApp.WHATSAPP
                    else -> CommunicationApp.SMS
                }
                parseCommunicationIntent(lowerText, app)
            }
            
            lowerText.contains("schedule") || lowerText.contains("book") || appointmentKeywords.any { lowerText.contains(it) } -> parseCalendarInsert(lowerText)
            
            else -> IntentResult.Unrecognized(text)
        }
    }

    private fun parseCallIntent(text: String): IntentResult {
        val simIndex = if (text.contains("sim 2")) 2 else 1
        val recipient = text.substringAfter("call ").substringBefore(" use").trim()
        return IntentResult.Call(recipient, simIndex)
    }

    private fun parseCommunicationIntent(text: String, app: CommunicationApp): IntentResult {
        val recipient = text.substringAfter("to ").substringBefore(" saying").substringBefore(" say").trim()
        val message = text.substringAfter("saying ").substringAfter("say ").trim()
        return IntentResult.SendMessage(app, recipient, if (message == text) "Hi" else message)
    }

    private fun parseCalendarInsert(text: String): IntentResult {
        val calendar = getCalendarForText(text)
        
        // Precise Time Extraction: Handles "3 pm", "3pm", "8:30 pm"
        val timeRegex = Regex("(\\d+)(?::(\\d+))?\\s*(pm|am)")
        val match = timeRegex.find(text)
        if (match != null) {
            var hour = match.groupValues[1].toInt()
            val minute = if (match.groupValues[2].isNotEmpty()) match.groupValues[2].toInt() else 0
            val amPm = match.groupValues[3]
            
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
        }

        val title = text.substringAfter("schedule ").substringAfter("book ").substringBefore(" on").substringBefore(" today").substringBefore(" at").trim().replaceFirstChar { it.uppercase() }
        return IntentResult.CalendarInsert(title = if (title.isEmpty()) "Meeting" else title, startTime = calendar.timeInMillis, durationMinutes = 60)
    }

    private fun parseCalendarRangeDelete(text: String) = IntentResult.CalendarRangeDelete(getCalendarForText(text).apply { set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis, getCalendarForText(text).apply { set(Calendar.HOUR_OF_DAY, 23) }.timeInMillis)

    private fun parseCalendarQuery(text: String) = IntentResult.CalendarQuery(getCalendarForText(text).apply { set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis, getCalendarForText(text).apply { set(Calendar.HOUR_OF_DAY, 23) }.timeInMillis)

    private fun getCalendarForText(text: String): Calendar {
        val calendar = Calendar.getInstance()
        
        // Check for specific date pattern (e.g. "30 january" or "2 feb")
        var dateFound = false
        for ((monthName, monthValue) in monthMap) {
            if (text.contains(monthName)) {
                calendar.set(Calendar.MONTH, monthValue)
                val dayMatch = Regex("(\\d+)").find(text.replace(monthName, " "))
                dayMatch?.let {
                    calendar.set(Calendar.DAY_OF_MONTH, it.groupValues[1].toInt())
                    dateFound = true
                }
                break
            }
        }

        if (!dateFound) {
            if (text.contains("tomorrow")) calendar.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            // If we found a specific date, ensure we don't accidentally add "tomorrow" offsets
            val now = Calendar.getInstance()
            if (calendar.before(now) && calendar.get(Calendar.MONTH) <= now.get(Calendar.MONTH)) {
                calendar.add(Calendar.YEAR, 1)
            }
        }
        return calendar
    }
}
