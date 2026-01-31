package com.example.myapplication.communication

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.*

class NotificationService : NotificationListenerService() {

    data class LoggedMessage(val sender: String, val text: String, val timestamp: Long, val app: String)

    companion object {
        private val messageLog = mutableListOf<LoggedMessage>()

        fun getMessagesAfter(timeMillis: Long): List<LoggedMessage> {
            return messageLog.filter { it.timestamp > timeMillis }
        }

        fun getLastMessageFrom(contact: String, app: String? = null): LoggedMessage? {
            return messageLog.filter { 
                it.sender.contains(contact, ignoreCase = true) && (app == null || it.app == app)
            }.lastOrNull()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName != "com.whatsapp" && packageName != "com.google.android.gm") return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        val appName = if (packageName == "com.whatsapp") "WhatsApp" else "Gmail"
        
        synchronized(messageLog) {
            messageLog.add(LoggedMessage(title, text, System.currentTimeMillis(), appName))
            // Keep only last 100 messages to save memory
            if (messageLog.size > 100) messageLog.removeAt(0)
        }
        
        Log.d("NotificationService", "Logged $appName message from $title: $text")
    }
}
