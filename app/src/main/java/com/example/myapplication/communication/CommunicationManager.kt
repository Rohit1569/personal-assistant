package com.example.myapplication.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.example.myapplication.plugin.CommunicationApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationManager @Inject constructor(private val context: Context) {

    fun sendMessage(app: CommunicationApp, recipient: String, message: String): Result<Unit> {
        return try {
            when (app) {
                CommunicationApp.WHATSAPP -> {
                    AutomationState.requestAutoSend()
                    launchIntent(getWhatsAppIntent(recipient, message))
                }
                CommunicationApp.SMS -> {
                    sendDirectSms(recipient, message)
                }
                CommunicationApp.GMAIL -> launchIntent(getEmailIntent(recipient, message, "com.google.android.gm"))
                CommunicationApp.HOTMAIL -> launchIntent(getEmailIntent(recipient, message, "com.microsoft.office.outlook"))
                CommunicationApp.AOL -> launchIntent(getEmailIntent(recipient, message, "com.aol.mobile.aolapp"))
                CommunicationApp.YAHOO -> launchIntent(getEmailIntent(recipient, message, "com.yahoo.mobile.client.android.mail"))
                CommunicationApp.SLACK -> launchIntent(getSlackIntent(message))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun initiateCall(recipient: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$recipient")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to Dial pad if CALL permission is missing or fails
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$recipient")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        }
    }

    private fun sendDirectSms(phone: String, message: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phone, null, message, null, null)
            }
            Log.d("SMS_BOT", "SMS sent successfully to $phone")
        } catch (e: Exception) {
            Log.e("SMS_BOT", "Failed to send SMS: ${e.message}")
            // Fallback to intent if permission or service fails
            launchIntent(Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
                putExtra("sms_body", message)
            })
        }
    }

    private fun getEmailIntent(email: String, message: String, packageName: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            setPackage(packageName)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Message from AI Assistant")
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    private fun launchIntent(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Intent.EXTRA_EMAIL, arrayOf(intent.getStringArrayExtra(Intent.EXTRA_EMAIL)?.firstOrNull() ?: ""))
                putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
            }
            context.startActivity(fallbackIntent)
        }
    }

    private fun getWhatsAppIntent(phone: String, message: String): Intent {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
        return Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.whatsapp") }
    }

    private fun getSlackIntent(message: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }
}
