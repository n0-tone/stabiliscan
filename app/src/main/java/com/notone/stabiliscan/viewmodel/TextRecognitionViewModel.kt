package com.notone.stabiliscan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
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

    private val _scanEvents = MutableSharedFlow<ScanEvent>()
    val scanEvents: SharedFlow<ScanEvent> = _scanEvents

    val savedTexts: StateFlow<List<String>> = preferenceManager.savedTexts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnboardingCompleted: StateFlow<Boolean?> = preferenceManager.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding() {
        viewModelScope.launch {
            preferenceManager.setOnboardingCompleted(true)
        }
    }

    fun recognizeText(bitmap: Bitmap) {
        if (isProcessing) return
        isProcessing = true
        
        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedText = visionText.text
                isProcessing = false
                viewModelScope.launch {
                    if (visionText.text.isNotBlank()) {
                        saveToHistory(visionText.text)
                        _scanEvents.emit(ScanEvent.Success(visionText.text))
                    } else {
                        _scanEvents.emit(ScanEvent.NoTextFound)
                    }
                }
            }
            .addOnFailureListener {
                recognizedText = "Error: ${it.message}"
                isProcessing = false
                viewModelScope.launch {
                    _scanEvents.emit(ScanEvent.Error(it.message ?: "Unknown error"))
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