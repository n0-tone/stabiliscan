package com.notone.stabiliscan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notone.stabiliscan.camera.CameraPreview
import com.notone.stabiliscan.sensor.StabilitySensor
import com.notone.stabiliscan.utils.toBitmapCompat
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel

@Composable
fun TextRecognitionScreen(
    viewModel: TextRecognitionViewModel,
    onTextCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val sensor = remember { StabilitySensor(context) }
    
    // Logic to only show stability indicator when there's likely text
    // For now, we use isProcessing as a proxy for "something is happening"
    // or we could add a "hasText" state to the sensor/preview logic.

    DisposableEffect(Unit) {
        sensor.start()
        onDispose { sensor.stop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview { imageProxy ->
            val bitmap = imageProxy.toBitmapCompat()

            if (bitmap != null) {
                if (sensor.isStable) {
                    viewModel.recognizeText(bitmap)
                }
            }
        }

        // Overlay UI for stability - Only shows when device is NOT stable OR when processing
        // "Pronto para ler" will only show if we are stable AND not already showing a result
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp) // Moved up as requested
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !sensor.isStable,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "⚠ Estabilize o dispositivo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // We removed the "Pronto para ler" persistent box to avoid clutter.
            // It was "always on screen" which the user disliked.
        }

        if (viewModel.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "A ler...", 
                        color = Color.White, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Trigger modal when text is found
        LaunchedEffect(viewModel.recognizedText) {
            if (viewModel.recognizedText.isNotBlank() && !viewModel.isProcessing) {
                onTextCaptured(viewModel.recognizedText)
            }
        }
    }
}
