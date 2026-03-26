package com.notone.stabiliscan.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notone.stabiliscan.viewmodel.TextRecognitionViewModel
import kotlinx.coroutines.launch

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

    // Listen to scan events for Toast and Modal
    LaunchedEffect(Unit) {
        viewModel.scanEvents.collect { event ->
            val message = when (event) {
                is TextRecognitionViewModel.ScanEvent.Success -> {
                    selectedTextForModal = event.text
                    null
                }
                is TextRecognitionViewModel.ScanEvent.NoTextFound -> {
                    "Nenhum texto detetado. Tente aproximar ou focar melhor."
                }
                is TextRecognitionViewModel.ScanEvent.Error -> {
                    "Erro: ${event.message}"
                }
            }
            
            message?.let {
                val toast = Toast.makeText(context, it, Toast.LENGTH_SHORT)
                // Set gravity to display toast higher (Center or specified offset)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
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
                            "StabiliScan", 
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Sobre a Aplicação",
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
                            "Esta ferramenta ajuda idosos com dificuldades visuais ou tremores leves. " +
                            "O objetivo é facilitar a leitura de documentos e rótulos, capturando texto apenas quando o dispositivo está estável.",
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
                        "Histórico de Leituras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (savedTexts.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "As suas capturas aparecerão aqui.", 
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
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
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.align(Alignment.End).padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Limpar Histórico")
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TextRecognitionScreen(
                viewModel = viewModel,
                onTextCaptured = { /* Handled by LaunchedEffect scanEvents */ }
            )

            // Sleek Minimalist Menu Button
            FilledIconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(24.dp)
                    .statusBarsPadding()
                    .size(64.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Histórico",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // High Accessibility Modal
    if (selectedTextForModal != null) {
        val currentText = selectedTextForModal!!
        AlertDialog(
            onDismissRequest = { selectedTextForModal = null },
            confirmButton = {
                Button(
                    onClick = { selectedTextForModal = null },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Voltar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = { 
                Text(
                    "Leitura Concluída", 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                ) 
            },
            text = {
                Column(modifier = Modifier.fillMaxHeight(0.8f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                                MaterialTheme.shapes.extraLarge
                            )
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentText,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.3).sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Ajustar Tamanho da Letra", 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 24f..100f,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("A", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}
