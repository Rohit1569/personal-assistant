package com.example.myapplication.communication

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper

class WhatsAppAutomationService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "com.whatsapp") return
        
        if (!AutomationState.isAutoSendRequested.get()) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        
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
                    Log.d("WHATSAPP_BOT", "Found send button, clicking...")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    foundAndClicked = true
                    break
                }
            }
            if (foundAndClicked) break
        }

        if (!foundAndClicked) {
            foundAndClicked = findAndClickByText(rootNode, "Send")
        }

        if (foundAndClicked) {
            AutomationState.consumeAutoSendRequest()
            
            // NEURAL RECOIL: Wait 800ms for message to send, then return to app
            Handler(Looper.getMainLooper()).postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
                // Second back if needed to exit the chat screen
                Handler(Looper.getMainLooper()).postDelayed({
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }, 300)
            }, 800)
        }
        
        rootNode.recycle()
    }

    private fun findAndClickByText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.contentDescription?.toString()?.equals(text, ignoreCase = true) == true ||
            node.text?.toString()?.equals(text, ignoreCase = true) == true) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickByText(child, text)) return true
        }
        return false
    }

    override fun onInterrupt() {}
}
