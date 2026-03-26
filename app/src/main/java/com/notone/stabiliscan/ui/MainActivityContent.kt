package com.notone.stabiliscan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val savedTexts by viewModel.savedTexts.collectAsState()
    
    var selectedTextForModal by remember { mutableStateOf<String?>(null) }
    var fontSize by remember { mutableFloatStateOf(32f) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxHeight()) {
                    Text(
                        "StabiliScan", 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Sobre a Aplicação",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        "Esta ferramenta ajuda idosos com dificuldades visuais ou tremores leves. " +
                        "O objetivo é facilitar a leitura de documentos e rótulos, capturando texto apenas quando o dispositivo está estável.",
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Histórico de Leituras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (savedTexts.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "As suas capturas aparecerão aqui.", 
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
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
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(
                                        text = text.take(80) + if (text.length > 80) "..." else "",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 16.sp,
                                        maxLines = 3,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    if (savedTexts.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Limpar Histórico", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TextRecognitionScreen(
                viewModel = viewModel,
                onTextCaptured = { text ->
                    selectedTextForModal = text
                }
            )

            // Sleek Minimalist Menu Button
            FilledIconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(24.dp)
                    .size(60.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Menu",
                    modifier = Modifier.size(30.dp)
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Voltar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = { 
                Text(
                    "Leitura Concluída", 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            text = {
                Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = currentText,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.3).sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Aumentar o tamanho da letra", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 24f..80f,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("A", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        )
    }
}
