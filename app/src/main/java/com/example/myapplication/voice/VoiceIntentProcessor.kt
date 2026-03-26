package com.example.myapplication.voice

import com.example.myapplication.plugin.CommunicationApp
import com.example.myapplication.plugin.IntentResult
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class VoiceIntentProcessor {

    private val appointmentKeywords = listOf(
        "nail", "haircut", "doctor", "plumber", "electrician", "specialist", 
        "client", "partner", "prospect", "real estate", "handyman", "roofer", 
        "siding", "patio", "designer", "fence", "mechanic", "sport", 
        "coach", "meeting", "appointment", "event", "मुलाकात", "बैठक"
    )

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }

    fun parse(text: String): IntentResult {
        val lowerText = text.lowercase()
            .replace(Regex("(\\d+)(st|nd|rd|th)"), "$1")
            .replace(",", "").trim()
        
        val currencyRegex = Regex("""([\$₹])\s?(\d+)|(\d+)\s?([\$₹]|dollars?|rupees?|bucks?|रुपये|रुपया)""")
        val currencyMatch = currencyRegex.find(lowerText)
        val extractedAmount = currencyMatch?.let { 
            it.groups[2]?.value?.toDoubleOrNull() ?: it.groups[3]?.value?.toDoubleOrNull() 
        } ?: 0.0

        return when {
            // --- 1. CAB & SHOPPING ---
            lowerText.containsAny("uber", "ola", "कैब", "टैक्सी") -> {
                val provider = if (lowerText.contains("ola")) "OLA" else "UBER"
                val destination = lowerText.substringAfter("to ").substringAfter("जाना है").trim()
                IntentResult.BookCab(provider, destination.ifEmpty { "your destination" })
            }

            lowerText.containsAny("amazon", "buy on", "खरीदना", "अमेज़न", "shop for") -> {
                val query = lowerText.replace("search for", "").replace("amazon", "").replace("buy", "").replace("खरीदना है", "").replace("shop for", "").replace("on", "").trim()
                IntentResult.Query("OPEN_APP|AMAZON|$query")
            }

            // --- 2. NAVIGATION & MAPS ---
            lowerText.containsAny("google maps", "navigate to", "directions to", "रास्ता", "मैप", "दिखाओ", "location of", "where is", "search for") && 
            lowerText.containsAny("map", "maps", "रास्ता", "मैप", "location") -> {
                val destination = lowerText
                    .replace("search for", "").replace("on google maps", "").replace("on maps", "").replace("on map", "")
                    .replace("navigate to", "").replace("directions to", "").replace("location of", "").replace("where is", "")
                    .replace("का रास्ता दिखाओ", "").replace("मैप पर दिखाओ", "").replace("जाना है", "").trim()
                IntentResult.Query("OPEN_MAPS|$destination")
            }

            // --- 3. WEB SEARCH & YOUTUBE ---
            lowerText.containsAny("youtube", "play on", "यूट्यूब", "चलाओ", "बजाओ", "video of") -> {
                val query = lowerText.replace("search for", "").replace("youtube", "").replace("play", "").replace("video of", "").replace("on", "").replace("चलाओ", "").replace("बजाओ", "").trim()
                IntentResult.Query("OPEN_APP|YOUTUBE|$query")
            }
            
            lowerText.containsAny("search for", "who is", "what is", "खोजो", "क्या है", "कौन है", "google", "about") -> {
                val query = lowerText.replace("search for", "").replace("google", "").replace("tell me about", "").replace("खोजो", "").replace("के बारे में", "").trim()
                IntentResult.Query("OPEN_BROWSER|$query")
            }

            // --- 4. COMMUNICATION (SMS / WhatsApp / Email) ---
            lowerText.containsAny("email", "mail", "ईमेल", "डाक") -> {
                val app = if (lowerText.contains("hotmail")) CommunicationApp.HOTMAIL else CommunicationApp.GMAIL
                val recipient = if (lowerText.contains(" को")) {
                    lowerText.substringBefore(" को").substringAfterLast(" ").trim()
                } else {
                    lowerText.substringAfter("to ").substringBefore(" saying").trim()
                }
                val message = lowerText.substringAfter("saying ").substringAfter("बोलो ").substringAfter("कहना ").trim()
                IntentResult.SendMessage(app, recipient, message)
            }

            lowerText.containsAny("whatsapp", "sms", "text", "संदेश", "भेजो", "मैसेज", "message", "ko") -> {
                val app = if (lowerText.contains("whatsapp")) CommunicationApp.WHATSAPP else CommunicationApp.SMS
                val recipient = when {
                    lowerText.contains(" को") -> lowerText.substringBefore(" को").trim().split(" ").last()
                    lowerText.contains(" ko") -> lowerText.substringBefore(" ko").trim().split(" ").last()
                    lowerText.contains(" to ") -> lowerText.substringAfter(" to ").substringBefore(" saying").substringBefore(" message").trim()
                    else -> lowerText.substringBefore(" ").trim()
                }
                val message = when {
                    lowerText.contains(" कि ") -> lowerText.substringAfter(" कि ").trim()
                    lowerText.contains(" ki ") -> lowerText.substringAfter(" ki ").trim()
                    lowerText.contains(" saying ") -> lowerText.substringAfter(" saying ").trim()
                    lowerText.contains(" message kro ") -> lowerText.substringAfter(" message kro ").trim()
                    else -> lowerText.substringAfter("message ").substringAfter("संदेश ").trim()
                }
                IntentResult.SendMessage(app, recipient, message)
            }

            // --- 5. FINANCE ---
            (currencyMatch != null || lowerText.containsAny("expense", "spent", "खर्च", "दिए", "kharch")) && !lowerText.containsAny("salary", "तनख्वाह") -> {
                val category = when {
                    lowerText.containsAny("food", "lunch", "खाना", "नाश्ता", "khana") -> "Food"
                    lowerText.containsAny("gas", "fuel", "पेट्रोल", "डीजल", "petrol") -> "Gas"
                    else -> "Shopping"
                }
                IntentResult.AddExpense(extractedAmount, category, lowerText.take(20))
            }

            // --- 6. PRODUCTIVITY ---
            lowerText.containsAny("remind me", "todo", "याद दिलाओ", "काम", "yaad dilao") -> {
                val task = lowerText.replace("remind me to ", "").replace("याद दिलाओ कि ", "").replace("yaad dilao ki ", "").replace("todo ", "").trim()
                IntentResult.AddTask(task)
            }
            
            lowerText.containsAny("note", "लिखो", "नोट", "likho") -> {
                val content = lowerText.replace("take a note ", "").replace("note down ", "").replace("लिखो कि ", "").replace("likho ki ", "").trim()
                IntentResult.AddNote("Voice Note", content)
            }

            // --- 7. CALLS ---
            lowerText.containsAny("call", "फोन करो", "phone kro") -> {
                val target = when {
                    lowerText.contains(" को") -> lowerText.substringBefore(" को").trim().split(" ").last()
                    lowerText.contains(" ko") -> lowerText.substringBefore(" ko").trim().split(" ").last()
                    else -> lowerText.substringAfter("call ").trim()
                }
                IntentResult.Call(target)
            }

            // --- 8. CALENDAR CRUD ---
            lowerText.containsAny("calendar", "schedule", "agenda", "appointment", "meeting", "बैठक", "तय करो", "कैैलेंडर") -> {
                when {
                    lowerText.containsAny("delete", "remove", "cancel", "हटाओ", "रद्द") -> {
                        val title = lowerText.replace("delete", "").replace("remove", "").replace("cancel", "").replace("meeting", "").replace("appointment", "").trim()
                        IntentResult.CalendarDelete(title)
                    }
                    lowerText.containsAny("any", "what is", "is there", "show", "agenda", "क्या है", "दिखाओ") -> {
                        val dateMillis = parseDate(lowerText)
                        IntentResult.CalendarQuery(dateMillis, dateMillis + 86400000)
                    }
                    else -> parseCalendarInsert(lowerText)
                }
            }

            else -> IntentResult.Unrecognized(text)
        }
    }

    private fun parseDate(text: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Tomorrow check
        if (text.contains("tomorrow") || text.contains("कल")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }

        // Today check
        if (text.contains("today") || text.contains("आज")) {
            return calendar.timeInMillis
        }

        // Specific Date parsing (e.g. 25th March)
        val monthMap = mapOf(
            "january" to 0, "february" to 1, "march" to 2, "april" to 3, "may" to 4, "june" to 5,
            "july" to 6, "august" to 7, "september" to 8, "october" to 9, "november" to 10, "december" to 11,
            "जनवरी" to 0, "फरवरी" to 1, "मार्च" to 2, "अप्रैल" to 3, "मई" to 4, "जून" to 5,
            "जुलाई" to 6, "अगस्त" to 7, "सितंबर" to 8, "अक्टूबर" to 9, "नवंबर" to 10, "दिसंबर" to 11
        )

        for ((monthName, monthIdx) in monthMap) {
            if (text.contains(monthName)) {
                val dayRegex = Regex("(\\d+)")
                val match = dayRegex.find(text)
                if (match != null) {
                    val day = match.groupValues[1].toInt()
                    calendar.set(Calendar.MONTH, monthIdx)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    // If the date has already passed this year, assume next year? No, usually current year.
                    return calendar.timeInMillis
                }
            }
        }

        return calendar.timeInMillis
    }

    private fun parseCalendarInsert(text: String): IntentResult {
        val calendar = Calendar.getInstance()
        
        // Extract Time
        val timeRegex = Regex("(\\d+)\\s?(am|pm|बजे)")
        val timeMatch = timeRegex.find(text)
        if (timeMatch != null) {
            var hour = timeMatch.groupValues[1].toInt()
            val marker = timeMatch.groupValues[2]
            if (marker == "pm" && hour < 12) hour += 12
            if (marker == "am" && hour == 12) hour = 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
        }

        // Extract Date
        val dateMillis = parseDate(text)
        val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))

        val title = text.substringAfter("schedule ").substringAfter("book ")
            .substringBefore(" at").substringBefore(" on").substringBefore(" को").trim()
        
        return IntentResult.CalendarInsert(
            title = if (title.isEmpty() || title == text) "Meeting" else title,
            startTime = calendar.timeInMillis,
            durationMinutes = 60
        )
    }
}
