package com.example.myapplication.communication

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val queue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                processQueue()
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            queue.add(text)
        }
    }

    private fun processQueue() {
        while (queue.isNotEmpty()) {
            val text = queue.removeAt(0)
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    fun shutDown() {
        tts?.stop()
        tts?.shutdown()
    }
}
