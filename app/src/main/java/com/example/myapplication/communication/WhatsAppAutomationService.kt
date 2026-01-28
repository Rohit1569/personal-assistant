package com.example.myapplication.communication

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAutomationService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only proceed if the event is from WhatsApp AND our app requested an auto-send
        if (event.packageName != "com.whatsapp") return
        
        // Use the shared state to decide whether to automate or not
        if (!AutomationState.isAutoSendRequested.get()) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        
        // WhatsApp Send Button detection logic
        val sendButtonIds = listOf(
            "com.whatsapp:id/send", 
            "com.whatsapp:id/voice_note_button",
            "com.whatsapp:id/entry_add"
        )

        var foundAndClicked = false
        for (id in sendButtonIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            for (node in nodes) {
                if (node.isClickable) {
                    Log.d("WHATSAPP_BOT", "Found send button by ID, clicking...")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    foundAndClicked = true
                    break
                }
            }
            if (foundAndClicked) break
        }

        if (!foundAndClicked) {
            // Fallback: Search by localized content description
            foundAndClicked = findAndClickByText(rootNode, "Send")
        }

        if (foundAndClicked) {
            // Success! Reset the flag so manual messages aren't sent automatically
            AutomationState.consumeAutoSendRequest()
        }
        
        rootNode.recycle()
    }

    private fun findAndClickByText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.contentDescription?.toString()?.equals(text, ignoreCase = true) == true ||
            node.text?.toString()?.equals(text, ignoreCase = true) == true) {
            if (node.isClickable) {
                Log.d("WHATSAPP_BOT", "Found send button by Text, clicking...")
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickByText(child, text)) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {}
}
