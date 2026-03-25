package com.notone.stabiliscan.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionViewModel : ViewModel() {

    var recognizedText by mutableStateOf("")
        private set

    fun recognizeText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener {
                recognizedText = it.text
            }
            .addOnFailureListener {
                recognizedText = "Error: ${it.message}"
            }
    }
}