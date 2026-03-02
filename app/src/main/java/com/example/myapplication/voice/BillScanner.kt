package com.example.myapplication.voice

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillScanner @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanBill(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val image = InputImage.fromFilePath(context, imageUri)
            val task = recognizer.process(image)
            // Use blocking await for clarity in this utility
            val result = Tasks.await(task)
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
