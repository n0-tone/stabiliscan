package com.notone.stabiliscan.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat

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
            onDismissRequest = { /* Bloqueia o fecho ao carregar fora */ },
            title = { Text("Permissão Necessária", fontWeight = FontWeight.Bold) },
            text = { Text("O StabiliScan precisa de aceder à câmara para poder ler texto. Por favor, conceda a permissão para continuar.") },
            confirmButton = {
                Button(
                    onClick = {
                        launcher.launch(permission)
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Dar permissão")
                }
            }
        )
    }
}
