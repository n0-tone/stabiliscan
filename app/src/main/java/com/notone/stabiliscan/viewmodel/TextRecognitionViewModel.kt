package com.notone.stabiliscan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.notone.stabiliscan.data.PreferenceManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TextRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)

    var recognizedText by mutableStateOf("")
        private set

    var isProcessing by mutableStateOf(false)
        private set

    // Real-time detected text blocks for overlay
    var detectedTextBlocks by mutableStateOf<List<Rect>>(emptyList())
        private set
    
    var lastAnalysisWidth by mutableIntStateOf(0)
    var lastAnalysisHeight by mutableIntStateOf(0)

    private val _scanEvents = MutableSharedFlow<ScanEvent>()
    val scanEvents: SharedFlow<ScanEvent> = _scanEvents

    val savedTexts: StateFlow<List<String>> = preferenceManager.savedTexts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnboardingCompleted: StateFlow<Boolean?> = preferenceManager.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun completeOnboarding() {
        viewModelScope.launch {
            preferenceManager.setOnboardingCompleted(true)
        }
    }

    fun updateDetectedBlocks(image: InputImage) {
        lastAnalysisWidth = image.width
        lastAnalysisHeight = image.height

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                detectedTextBlocks = visionText.textBlocks.mapNotNull { it.boundingBox }
            }
            .addOnFailureListener {
                detectedTextBlocks = emptyList()
            }
    }

    fun recognizeText(bitmap: Bitmap) {
        if (isProcessing) return
        isProcessing = true
        recognizedText = "" // Clear previous text to avoid showing stale results
        
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                recognizedText = resultText
                isProcessing = false
                viewModelScope.launch {
                    if (resultText.isNotBlank()) {
                        saveToHistory(resultText)
                        _scanEvents.emit(ScanEvent.Success(resultText))
                    } else {
                        _scanEvents.emit(ScanEvent.NoTextFound)
                    }
                }
            }
            .addOnFailureListener {
                val errorMessage = it.message ?: "Unknown error"
                recognizedText = "Error: $errorMessage"
                isProcessing = false
                viewModelScope.launch {
                    _scanEvents.emit(ScanEvent.Error(errorMessage))
                }
            }
    }

    private fun saveToHistory(text: String) {
        viewModelScope.launch {
            preferenceManager.saveText(text)
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            preferenceManager.clearHistory()
        }
    }

    sealed class ScanEvent {
        data class Success(val text: String) : ScanEvent()
        object NoTextFound : ScanEvent()
        data class Error(val message: String) : ScanEvent()
    }
}