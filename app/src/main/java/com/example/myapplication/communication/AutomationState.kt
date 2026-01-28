package com.example.myapplication.communication

import java.util.concurrent.atomic.AtomicBoolean

object AutomationState {
    val isAutoSendRequested = AtomicBoolean(false)
    
    fun requestAutoSend() {
        isAutoSendRequested.set(true)
    }
    
    fun consumeAutoSendRequest(): Boolean {
        return isAutoSendRequested.getAndSet(false)
    }
}
