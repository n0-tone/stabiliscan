package com.notone.stabiliscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notone.stabiliscan.ui.CameraPermissionScreen
import com.notone.stabiliscan.ui.MainActivityContent
import com.notone.stabiliscan.ui.OnboardingScreen
import com.notone.stabiliscan.ui.theme.StabiliScanTheme
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StabiliScanTheme {
                val viewModel: TextRecognitionViewModel = viewModel()
                val onboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = false)

                if (!onboardingCompleted) {
                    OnboardingScreen(onFinished = {
                        viewModel.completeOnboarding()
                    })
                } else {
                    CameraPermissionScreen {
                        MainActivityContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
