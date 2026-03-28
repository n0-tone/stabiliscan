package com.notone.stabiliscan.camera

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.notone.stabiliscan.R

@Composable
fun CameraPermissionHandler(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permission = Manifest.permission.CAMERA
    var showDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
            showDialog = false
        } else {
            showDialog = true
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
            text = { Text(stringResource(R.string.permission_required_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        launcher.launch(permission)
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        )
    }
}
