package com.example.myapplication.voice

import com.example.myapplication.plugin.CommunicationApp
import com.example.myapplication.plugin.IntentResult
import java.util.Calendar

class VoiceIntentProcessor {

    fun parse(text: String): IntentResult {
        val lowerText = text.lowercase().replace(",", "").replace(".", "").trim()
        
        return when {
            // CHECK LAST MESSAGE
            lowerText.contains("last message") || lowerText.contains("check message") -> {
                val app = when {
                    lowerText.contains("whatsapp") -> CommunicationApp.WHATSAPP
                    lowerText.contains("mail") || lowerText.contains("gmail") || lowerText.contains("email") -> CommunicationApp.GMAIL
                    else -> CommunicationApp.WHATSAPP
                }
                val contactName = lowerText.substringAfter("from ").substringBefore(" in").substringBefore(" on").trim()
                IntentResult.LastMessageQuery(app, contactName)
            }

            // CALENDAR QUERY (Check meetings)
            lowerText.contains("any meeting") || lowerText.contains("what is scheduled") || lowerText.contains("check my calendar") -> {
                parseCalendarQuery(lowerText)
            }

            // CALENDAR DELETE (Cancel All)
            lowerText.contains("cancel all") && (lowerText.contains("meeting") || lowerText.contains("appointment")) -> {
                parseCalendarRangeDelete(lowerText)
            }

            // CALENDAR DELETE (Cancel specific)
            lowerText.contains("cancel") && (lowerText.contains("meeting") || lowerText.contains("appointment")) -> {
                val title = lowerText.substringAfter("cancel ").replace("meeting", "").replace("appointment", "").trim()
                IntentResult.CalendarDelete(title)
            }

            // GMAIL / EMAIL INTENTS
            lowerText.contains("email") || lowerText.contains("gmail") || lowerText.contains("mail") -> {
                parseCommunicationIntent(lowerText, CommunicationApp.GMAIL)
            }

            // WHATSAPP / SMS INTENTS
            lowerText.contains("whatsapp") || lowerText.contains("message") -> {
                val app = if (lowerText.contains("whatsapp")) CommunicationApp.WHATSAPP else CommunicationApp.SMS
                parseCommunicationIntent(lowerText, app)
            }
            
            // CALL INTENTS
            lowerText.contains("call") -> {
                parseCallIntent(lowerText)
            }
            
            // AUTOMATED CALENDAR INTENTS
            lowerText.contains("schedule") || lowerText.contains("meeting") || lowerText.contains("book") -> {
                parseCalendarIntent(lowerText)
            }
            
            else -> IntentResult.Unrecognized(text)
        }
    }

    private fun parseCalendarRangeDelete(text: String): IntentResult {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        
        if (text.contains("tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val end = calendar.timeInMillis
        
        return IntentResult.CalendarRangeDelete(start, end)
    }

    private fun parseCalendarQuery(text: String): IntentResult {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        
        if (text.contains("tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val end = calendar.timeInMillis
        
        return IntentResult.CalendarQuery(start, end)
    }

    private fun parseCallIntent(text: String): IntentResult {
        val words = text.split(" ")
        val callIndex = words.indexOf("call")
        val simIndex = if (text.contains("sim 2") || text.contains("sim2")) 2 else 1
        
        val recipient = if (callIndex != -1 && callIndex + 1 < words.size) {
            val endIdx = listOf(words.indexOf("use"), words.indexOf("sim"), words.indexOf("in"))
                .filter { it > callIndex }.minOrNull() ?: (callIndex + 2)
            words.subList(callIndex + 1, minOf(endIdx, words.size)).joinToString(" ")
        } else "Contact"

        return IntentResult.Call(recipient, simIndex)
    }

    private fun parseCommunicationIntent(text: String, app: CommunicationApp): IntentResult {
        val words = text.split(" ")
        val toIndex = words.indexOf("to")
        val msgStartIndex = when {
            words.contains("saying") -> words.indexOf("saying")
            words.contains("say") -> words.indexOf("say")
            else -> -1
        }

        var recipient = if (toIndex != -1 && toIndex + 1 < words.size) {
            val endIdx = if (msgStartIndex != -1) msgStartIndex else minOf(toIndex + 3, words.size)
            words.subList(toIndex + 1, endIdx).joinToString(" ")
                .replace("in whatsapp", "").replace("on whatsapp", "").trim()
        } else "Contact"

        val message = if (msgStartIndex != -1 && msgStartIndex + 1 < words.size) {
            words.subList(msgStartIndex + 1, words.size).joinToString(" ")
        } else "Hi"

        return IntentResult.SendMessage(app, recipient, message)
    }

    private fun parseCalendarIntent(text: String): IntentResult {
        val calendar = Calendar.getInstance()
        if (text.contains("tomorrow")) calendar.add(Calendar.DAY_OF_YEAR, 1)
        
        val timeRegex = Regex("(\\d+)(?::(\\d+))?\\s*(pm|am)?")
        val match = timeRegex.find(text)
        
        if (match != null) {
            var hour = match.groupValues[1].toInt()
            val minute = if (match.groupValues[2].isNotEmpty()) match.groupValues[2].toInt() else 0
            val amPm = match.groupValues[3].lowercase()
            
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        val rawTitle = when {
            text.contains("schedule") -> text.substringAfter("schedule")
            text.contains("book") -> text.substringAfter("book")
            else -> "Meeting"
        }
        
        val cleanTitle = rawTitle.substringBefore(" today").substringBefore(" tomorrow").substringBefore(" at").trim()

        return IntentResult.CalendarInsert(
            title = if (cleanTitle.isEmpty()) "AI Appointment" else cleanTitle,
            startTime = calendar.timeInMillis,
            durationMinutes = 60
        )
    }
}
