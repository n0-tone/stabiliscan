package com.notone.stabiliscan.ui

import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notone.stabiliscan.utils.toBitmapCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.notone.stabiliscan.sensor.StabilitySensor
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel

@Composable
fun TextRecognitionScreen(
    viewModel: TextRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensor = remember { StabilitySensor(context) }

    val text = viewModel.recognizedText

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
                        "Hold device steady",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Warning UI
        if (!sensor.isStable) {
            Text(
                "⚠ Hold device steady",
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }

        // Result
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            color = Color.White
        )
    }
}