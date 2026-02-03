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
        val lowerText = text.lowercase()
            .replace(Regex("(\\d+)(st|nd|rd|th)"), "$1")
            .replace(",", "").replace(".", "").trim()
        
        return when {
            // 1. CALENDAR DELETE (Highest Priority)
            lowerText.contains("cancel all") || lowerText.contains("clear all") || lowerText.contains("delete all") -> {
                parseCalendarRangeDelete(lowerText)
            }
            
            lowerText.startsWith("cancel") || lowerText.startsWith("delete") -> {
                val title = lowerText.substringAfter("cancel ").substringAfter("delete ")
                    .replace("meetings", "").replace("meeting", "").replace("appointments", "").replace("appointment", "").trim()
                IntentResult.CalendarDelete(title)
            }

            // 2. NAVIGATION & MAPS
            lowerText.contains("google maps") || lowerText.contains("maps") || 
            lowerText.contains("where is") || lowerText.contains("location of") || 
            lowerText.contains("navigate to") || lowerText.contains("directions to") -> {
                val destination = lowerText
                    .replace("google maps", "").replace("on map", "").replace("on maps", "")
                    .substringAfter("where is ").substringAfter("location of ")
                    .substringAfter("navigate to ").substringAfter("directions to ").trim()
                IntentResult.Query("OPEN_MAPS|$destination")
            }

            // 3. BROWSER & SEARCH
            lowerText.contains("search for") || lowerText.contains("who is") || 
            lowerText.contains("what is") || lowerText.contains("nearest") || 
            lowerText.contains("find") || lowerText.contains("browse") -> {
                if (!lowerText.contains("meeting") && !lowerText.contains("schedule")) {
                    val query = lowerText.substringAfter("search for ").substringAfter("find ")
                        .substringAfter("browse ").substringAfter("what is ").substringAfter("who is ").trim()
                    IntentResult.Query("OPEN_BROWSER|$query")
                } else {
                    parseCalendarQuery(lowerText)
                }
            }

            // 4. CAB BOOKING
            lowerText.contains("uber") || lowerText.contains("ola") -> {
                val provider = if (lowerText.contains("ola")) "OLA" else "UBER"
                var dest = "your destination"
                val markers = listOf(" to ", " for ", " at ")
                for (marker in markers) {
                    if (lowerText.contains(marker)) {
                        dest = lowerText.substringAfter(marker).trim()
                        break
                    }
                }
                if (dest.contains("railway station") || dest.contains("station")) {
                    dest = "Pune Station"
                }
                IntentResult.BookCab(provider, dest)
            }

            // 5. CALL INTENTS
            lowerText.startsWith("call") -> parseCallIntent(lowerText)

            // 6. CALENDAR QUERY
            lowerText.contains("any meeting") || lowerText.contains("what is scheduled") || lowerText.contains("check my calendar") -> {
                parseCalendarQuery(lowerText)
            }

            // 7. COMMUNICATION
            lowerText.contains("send") || lowerText.contains("mail") || lowerText.contains("whatsapp") || lowerText.contains("sms") || lowerText.contains("text") -> {
                val app = when {
                    lowerText.contains("mail") || lowerText.contains("gmail") -> CommunicationApp.GMAIL
                    lowerText.contains("whatsapp") -> CommunicationApp.WHATSAPP
                    lowerText.contains("sms") || lowerText.contains("text") -> CommunicationApp.SMS
                    else -> CommunicationApp.WHATSAPP
                }
                parseCommunicationIntent(lowerText, app)
            }
            
            // 8. SCHEDULING
            lowerText.contains("schedule") || lowerText.contains("book") || appointmentKeywords.any { lowerText.contains(it) } -> {
                parseCalendarInsert(lowerText)
            }
            
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
        val timeRegex = Regex("(\\d+)(?::(\\d+))?\\s*(pm|am)")
        val match = timeRegex.find(text)
        if (match != null) {
            var hour = match.groupValues[1].toInt()
            val amPm = match.groupValues[3]
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, if (match.groupValues[2].isNotEmpty()) match.groupValues[2].toInt() else 0)
        }
        val title = text.substringAfter("schedule ").substringAfter("book ").substringBefore(" on").substringBefore(" today").substringBefore(" at").trim().replaceFirstChar { it.uppercase() }
        
        val invitee = if (text.contains("with ")) text.substringAfter("with ").substringBefore(" on").substringBefore(" at").trim() else null
        
        return IntentResult.CalendarInsert(
            title = if (title.isEmpty()) "Meeting" else title, 
            startTime = calendar.timeInMillis, 
            durationMinutes = 60,
            inviteeEmail = invitee 
        )
    }

    private fun parseCalendarRangeDelete(text: String): IntentResult {
        val calendar = getCalendarForText(text)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return IntentResult.CalendarRangeDelete(start, end)
    }

    private fun parseCalendarQuery(text: String): IntentResult {
        val calendar = getCalendarForText(text)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return IntentResult.CalendarQuery(start, end)
    }

    private fun getCalendarForText(text: String): Calendar {
        val calendar = Calendar.getInstance()
        var dateFound = false
        for ((monthName, monthValue) in monthMap) {
            if (text.contains(monthName)) {
                calendar.set(Calendar.MONTH, monthValue)
                Regex("(\\d+)").find(text.replace(monthName, " "))?.let {
                    calendar.set(Calendar.DAY_OF_MONTH, it.groupValues[1].toInt())
                    dateFound = true
                }
                break
            }
        }
        if (!dateFound && text.contains("tomorrow")) calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar
    }
}
