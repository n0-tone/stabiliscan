package com.notone.stabiliscan.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.notone.stabiliscan.R

@Composable
fun CameraPermissionHandler(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permission = Manifest.permission.CAMERA
    var showDialog by remember { mutableStateOf(false) }
    var goToSettings by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
            showDialog = false
        } else {
            showDialog = true
            val activity = context as? Activity
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: false
            
            // If we shouldn't show rationale and permission is still not granted,
            // it means it was denied with "Don't ask again" or similar.
            if (!showRationale) {
                goToSettings = true
            }
        }
    }

    // Check permission whenever the app comes to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted()
                    showDialog = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            launcher.launch(permission)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.permission_required_title), fontWeight = FontWeight.Bold) },
            text = { 
                Text(
                    if (goToSettings) stringResource(R.string.permission_denied_permanently_desc)
                    else stringResource(R.string.permission_required_desc)
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goToSettings) {
                            openAppSettings(context)
                        } else {
                            launcher.launch(permission)
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (goToSettings) stringResource(R.string.open_settings)
                        else stringResource(R.string.grant_permission)
                    )
                }
            }
        )
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
