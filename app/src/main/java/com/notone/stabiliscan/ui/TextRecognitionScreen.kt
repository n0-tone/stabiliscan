package com.notone.stabiliscan.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                } else {
                    Toast.makeText(
                        context,
                        "Mantenha o telemóvel parado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Overlay UI for stability
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!sensor.isStable) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "⚠ Estabilize o dispositivo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "✓ Pronto para ler",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (viewModel.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        
        // When text is recognized, we trigger the callback to show the modal
        LaunchedEffect(viewModel.recognizedText) {
            if (viewModel.recognizedText.isNotBlank() && !viewModel.isProcessing) {
                onTextCaptured(viewModel.recognizedText)
            }
        }
    }
}
