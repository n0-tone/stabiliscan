package com.notone.stabiliscan.ui

import androidx.compose.runtime.*

@Composable
fun CameraPermissionScreen() {
    var granted by remember { mutableStateOf(false) }

    CameraPermissionHandler {
        granted = true
    }

    if (granted) {
        TextRecognitionScreen()
    }
}