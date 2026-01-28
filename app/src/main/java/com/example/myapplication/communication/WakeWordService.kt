package com.example.myapplication.communication

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class WakeWordService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val wakeWord = "kiwi"
    private lateinit var recognizerIntent: Intent
    private var isCurrentlyListening = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        initSpeechRecognizer()
    }

    private fun startForegroundService() {
        val channelId = "kiwi_ai_listener"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "KIWI Wake Word", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("KIWI is Online")
            .setContentText("Listening for 'Kiwi'...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        startForeground(104, notification)
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isCurrentlyListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isCurrentlyListening = false }
            
            override fun onError(error: Int) {
                isCurrentlyListening = false
                // Force restart immediately on timeout (Error 6) or No Match (Error 7)
                mainHandler.postDelayed({ startListeningLoop() }, 200)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                checkWakeWord(text)
                isCurrentlyListening = false
                startListeningLoop()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                // SIRI JUMP: Detect wake word instantly
                if (text.contains(wakeWord)) {
                    checkWakeWord(text)
                    speechRecognizer?.stopListening()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        startListeningLoop()
    }

    private fun checkWakeWord(text: String) {
        if (text.contains(wakeWord)) {
            Log.d("KiwiBot", "Wake Word Detected!")
            playWakeTone()
            triggerVibration()
            
            // Extract the command after the word "kiwi"
            val command = text.substringAfter(wakeWord).trim()
            
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                putExtra("VOICE_COMMAND", if (command.isEmpty()) null else command)
            }
            startActivity(intent)
        }
    }

    private fun startListeningLoop() {
        mainHandler.post {
            if (!isCurrentlyListening) {
                try {
                    speechRecognizer?.startListening(recognizerIntent)
                } catch (e: Exception) {
                    mainHandler.postDelayed({ startListeningLoop() }, 1000)
                }
            }
        }
    }

    private fun playWakeTone() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun triggerVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        toneGenerator.release()
    }
}
