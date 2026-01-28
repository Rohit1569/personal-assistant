package com.example.myapplication.communication

import android.content.Context
import android.provider.ContactsContract
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ContactHelper @Inject constructor(private val context: Context) {

    data class ContactMatch(val name: String, val phone: String?, val email: String?)

    /**
     * Searches for a contact by name and retrieves both phone and email.
     * Uses fuzzy matching for robust recognition.
     */
    fun findContact(spokenName: String): ContactMatch? {
        val allContacts = getAllContacts()
        val cleanSpoken = spokenName.lowercase().trim()

        var bestMatchName: String? = null
        var minDistance = Int.MAX_VALUE
        val threshold = 2

        for (contactName in allContacts.keys) {
            val distance = calculateLevenshteinDistance(cleanSpoken, contactName.lowercase())
            if (distance < minDistance && distance <= threshold) {
                minDistance = distance
                bestMatchName = contactName
            }
        }

        return bestMatchName?.let { name ->
            val details = allContacts[name]
            ContactMatch(name, details?.first, details?.second)
        }
    }

    private fun getAllContacts(): Map<String, Pair<String?, String?>> {
        val contactsMap = mutableMapOf<String, Pair<String?, String?>>()
        val cr = context.contentResolver
        
        val cursor = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            val idIdx = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getString(idIdx)
                val name = it.getString(nameIdx)?.lowercase()?.trim() ?: continue

                // Get Phone
                var phone: String? = null
                val pCursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )
                pCursor?.use { pc -> if (pc.moveToFirst()) phone = pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) }

                // Get Email
                var email: String? = null
                val eCursor = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )
                eCursor?.use { ec -> if (ec.moveToFirst()) email = ec.getString(ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)) }

                contactsMap[name] = Pair(phone, email)
            }
        }
        return contactsMap
    }

    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }
}
