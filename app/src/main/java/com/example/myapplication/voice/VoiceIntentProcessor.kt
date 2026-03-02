package com.example.myapplication.voice

import com.example.myapplication.plugin.CommunicationApp
import com.example.myapplication.plugin.IntentResult
import java.util.Calendar

class VoiceIntentProcessor {

    private val appointmentKeywords = listOf(
        "nail", "haircut", "doctor", "plumber", "electrician", "specialist", 
        "client", "partner", "prospect", "real estate", "handyman", "roofer", 
        "siding", "patio", "designer", "fence", "mechanic", "sport", 
        "coach", "meeting", "appointment", "event"
    )

    fun parse(text: String): IntentResult {
        val lowerText = text.lowercase()
            .replace(Regex("(\\d+)(st|nd|rd|th)"), "$1")
            .replace(",", "").trim()
        
        val currencyRegex = Regex("""([\$₹])\s?(\d+)|(\d+)\s?([\$₹]|dollars?|rupees?|bucks?)""")
        val currencyMatch = currencyRegex.find(lowerText)
        val hasCurrency = currencyMatch != null
        val extractedAmount = currencyMatch?.let { 
            it.groups[2]?.value?.toDoubleOrNull() ?: it.groups[3]?.value?.toDoubleOrNull() 
        } ?: 0.0

        return when {
            // --- 1. CAB & SHOPPING (High Priority Actions) ---
            lowerText.contains("uber") || lowerText.contains("ola") -> {
                val provider = if (lowerText.contains("ola")) "OLA" else "UBER"
                val destination = lowerText.substringAfter("to ").trim()
                IntentResult.BookCab(provider, destination.ifEmpty { "your destination" })
            }
            
            lowerText.contains("amazon") || lowerText.contains("buy on") -> {
                val query = lowerText.substringAfter("amazon ").replace("search for", "").trim()
                IntentResult.Query("OPEN_APP|AMAZON|$query")
            }

            // --- 2. NAVIGATION & MAPS ---
            lowerText.contains("google maps") || lowerText.contains("navigate to") || lowerText.contains("directions to") -> {
                val destination = lowerText.substringAfter("navigate to ").substringAfter("directions to ").substringAfter("to ").trim()
                IntentResult.Query("OPEN_MAPS|$destination")
            }

            // --- 3. WEB SEARCH & YOUTUBE ---
            lowerText.contains("youtube") || lowerText.contains("play on") -> {
                val query = lowerText.substringAfter("youtube ").replace("search for", "").trim()
                IntentResult.Query("OPEN_APP|YOUTUBE|$query")
            }
            
            lowerText.contains("search for") || lowerText.contains("who is") || lowerText.contains("what is") -> {
                val query = lowerText.replace("search for", "").replace("google", "").trim()
                IntentResult.Query("OPEN_BROWSER|$query")
            }

            // --- 4. FINANCE ---
            (currencyMatch != null || lowerText.contains("expense") || lowerText.contains("spent")) && !lowerText.contains("salary") -> {
                val category = when {
                    lowerText.contains("food") || lowerText.contains("lunch") -> "Food"
                    lowerText.contains("gas") || lowerText.contains("fuel") -> "Gas"
                    else -> "Shopping"
                }
                IntentResult.AddExpense(extractedAmount, category, lowerText.take(20))
            }
            
            lowerText.contains("income") || lowerText.contains("salary") -> {
                IntentResult.AddIncome(extractedAmount, "Salary")
            }

            // --- 5. COMMUNICATION ---
            lowerText.contains("email") || lowerText.contains("mail") -> {
                val app = when {
                    lowerText.contains("hotmail") -> CommunicationApp.HOTMAIL
                    lowerText.contains("yahoo") -> CommunicationApp.YAHOO
                    lowerText.contains("aol") -> CommunicationApp.AOL
                    else -> CommunicationApp.GMAIL
                }
                val recipient = lowerText.substringAfter("to ").substringBefore(" saying").trim()
                val message = lowerText.substringAfter("saying ").trim()
                IntentResult.SendMessage(app, recipient, message)
            }

            // UPDATED: Added "sms" and broadened "text" keywords
            lowerText.contains("whatsapp") || lowerText.contains("sms") || lowerText.contains("text") -> {
                val app = if (lowerText.contains("whatsapp")) CommunicationApp.WHATSAPP else CommunicationApp.SMS
                val recipient = lowerText.substringAfter("to ").substringBefore(" saying").trim()
                val message = lowerText.substringAfter("saying ").trim()
                IntentResult.SendMessage(app, recipient, message)
            }

            // --- 6. PRODUCTIVITY ---
            lowerText.contains("remind me to") || lowerText.contains("todo") -> {
                IntentResult.AddTask(lowerText.replace("remind me to ", "").replace("todo ", "").trim())
            }
            
            lowerText.contains("take a note") || lowerText.contains("note down") -> {
                IntentResult.AddNote("Voice Note", lowerText.replace("take a note ", "").replace("note down ", "").trim())
            }

            // --- 7. SCHEDULING & CALLS ---
            lowerText.startsWith("call") -> IntentResult.Call(lowerText.substringAfter("call ").trim())
            
            lowerText.contains("schedule") || appointmentKeywords.any { lowerText.contains(it) } -> parseCalendarInsert(lowerText)

            else -> IntentResult.Unrecognized(text)
        }
    }

    private fun parseCalendarInsert(text: String): IntentResult {
        val calendar = Calendar.getInstance()
        val title = text.substringAfter("schedule ").substringAfter("book ").substringBefore(" at").trim()
        return IntentResult.CalendarInsert(title.ifEmpty { "Meeting" }, calendar.timeInMillis, 60)
    }
}
