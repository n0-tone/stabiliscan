package com.notone.stabiliscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notone.stabiliscan.camera.CameraPermissionScreen
import com.notone.stabiliscan.ui.MainActivityContent
import com.notone.stabiliscan.ui.OnboardingScreen
import com.notone.stabiliscan.ui.theme.StabiliScanTheme
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Hide status bar and navigation bar (Hide top and bottom bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            val viewModel: TextRecognitionViewModel = viewModel()
            val onboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

            // Keep the splash screen on-screen until we know if onboarding is completed
            splashScreen.setKeepOnScreenCondition {
                onboardingCompleted == null
            }

            StabiliScanTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    when (onboardingCompleted) {
                        null -> {
                            // Still loading, splash screen is visible
                        }
                        false -> {
                            OnboardingScreen(onFinished = {
                                viewModel.completeOnboarding()
                            })
                        }
                        true -> {
                            CameraPermissionScreen {
                                MainActivityContent(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
