package com.example.myapplication.communication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeuralMicManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onResult: ((String) -> Unit)? = null
    private var onStateChange: ((Boolean) -> Unit)? = null

    fun startListening(onState: (Boolean) -> Unit, onResultFound: (String) -> Unit) {
        this.onResult = onResultFound
        this.onStateChange = onState

        if (speechRecognizer != null) {
            speechRecognizer?.destroy()
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onState(true) }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { onState(false) }
            override fun onError(error: Int) { 
                Log.e("NeuralMic", "Error: $error")
                onState(false) 
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onResult?.invoke(it) }
                onState(false)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onStateChange?.invoke(false)
    }
}
