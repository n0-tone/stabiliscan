package com.notone.stabiliscan.ui

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notone.stabiliscan.R
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel
import kotlinx.coroutines.launch

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    viewModel: TextRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val savedTexts by viewModel.savedTexts.collectAsState()

    var selectedTextForModal by remember { mutableStateOf<String?>(null) }
    var fontSize by remember { mutableFloatStateOf(32f) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // ✅ Resolve ALL strings in composable scope
    val noTextFoundMessage = stringResource(R.string.no_text_found)

    // Listen to scan events
    LaunchedEffect(Unit) {
        viewModel.scanEvents.collect { event ->
            val message = when (event) {
                is TextRecognitionViewModel.ScanEvent.Success -> {
                    selectedTextForModal = event.text
                    null
                }

                is TextRecognitionViewModel.ScanEvent.NoTextFound -> {
                    noTextFoundMessage
                }

                is TextRecognitionViewModel.ScanEvent.Error -> {
                    // ✅ SAFE: use context here instead of stringResource
                    context.getString(R.string.error_prefix, event.message)
                }
            }

            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).apply {
                    setGravity(Gravity.CENTER, 0, 0)
                }.show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.85f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                drawerTonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxHeight()) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.app_name),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        stringResource(R.string.about_app),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.app_description),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        stringResource(R.string.reading_history),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (savedTexts.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.empty_history),
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(savedTexts.reversed()) { text ->
                                Card(
                                    onClick = {
                                        selectedTextForModal = text
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(
                                        text = text.take(80) + if (text.length > 80) "..." else "",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 16.sp,
                                        maxLines = 3,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (savedTexts.isNotEmpty()) {
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(stringResource(R.string.clear_history))
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            TextRecognitionScreen(
                viewModel = viewModel,
                onTextCaptured = {}
            )

            FilledIconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(24.dp)
                    .statusBarsPadding()
                    .size(64.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(R.string.history_icon_desc),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // Confirm dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.clear_history_confirmation_title)) },
            text = { Text(stringResource(R.string.clear_history_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Modal
    selectedTextForModal?.let { currentText ->
        Dialog(
            onDismissRequest = { selectedTextForModal = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {

                    Text(
                        stringResource(R.string.reading_completed),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Box(
                        Modifier.weight(1f)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentText,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.3).sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 12f..100f
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { selectedTextForModal = null },
                        modifier = Modifier.fillMaxWidth().height(72.dp)
                    ) {
                        Text(stringResource(R.string.back), fontSize = 22.sp)
                    }
                }
            }
        }
    }
}