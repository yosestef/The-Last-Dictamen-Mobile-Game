package com.android.mobile.games.app.games.fruitninja.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.games.fruitninja.data.RetrofitGameService
import com.android.mobile.games.app.ui.theme.*
import com.android.mobile.games.app.ui.util.HideSystemBars
import kotlinx.coroutines.launch

@Composable
fun FruitNinjaMenuScreen(
    onStartGameClick: (FruitNinjaDifficulty, String) -> Unit,
    onBackClick: () -> Unit
) {
    HideSystemBars()
    var selectedDifficulty by remember { mutableStateOf(FruitNinjaDifficulty.SAVE_SEMESTER) }
    var username by remember { mutableStateOf("") }
    var showHelpModal by remember { mutableStateOf(false) }
    var showRankingModal by remember { mutableStateOf(false) }
    var rankingData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }

    val gameService = remember { RetrofitGameService() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. FONDO DE PORTADA
        Image(
            painter = painterResource(id = R.drawable.portada_slash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. BOTONES SUPERIORES (AYUDA Y RANKING)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Ranking
            IconButton(
                onClick = {
                    showRankingModal = true
                    isLoadingRanking = true
                    coroutineScope.launch {
                        rankingData = gameService.getRanking()
                        isLoadingRanking = false
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CuteYellow,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("🏆", fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón Ayuda
            IconButton(
                onClick = { showHelpModal = true },
                modifier = Modifier.size(56.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CuteLavender,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("?", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. PANEL DE CONTROL (Super cute glassmorphic pink)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = CuteCream.copy(alpha = 0.9f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            border = BorderStroke(2.dp, CutePink)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌸 CODE SLASHER 🌸",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Campo de Nombre de Usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre del programador", color = TextDark.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(0.85f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextDark,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CutePink,
                        unfocusedBorderColor = CutePink.copy(alpha = 0.5f),
                        cursorColor = CutePink,
                        focusedLabelColor = CutePink,
                        unfocusedLabelColor = TextDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Modos
                ModeSelectorRow(
                    selected = selectedDifficulty,
                    onSelected = { selectedDifficulty = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Principal
                Button(
                    onClick = { 
                        if (username.isNotBlank()) {
                            onStartGameClick(selectedDifficulty, username) 
                        }
                    },
                    enabled = username.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CutePink,
                        disabledContainerColor = Color.LightGray,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "✨ ¡A COMPILAR! ✨",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN CERRAR LAB
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    border = BorderStroke(1.5.dp, CuteLavender),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "CERRAR LABORATORIO 🎀",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }
            }
        }

        // Modal de Ranking
        if (showRankingModal) {
            AlertDialog(
                onDismissRequest = { showRankingModal = false },
                containerColor = CuteCream,
                title = { Text("👑 TOP PROGRAMADORES 👑", color = TextDark, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        if (isLoadingRanking) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = CutePink)
                        } else if (rankingData.isEmpty()) {
                            Text("No hay registros aún.", color = TextDark)
                        } else {
                            rankingData.forEachIndexed { index, pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val emoji = when(index) {
                                        0 -> "🥇 "
                                        1 -> "🥈 "
                                        2 -> "🥉 "
                                        else -> "${index + 1}. "
                                    }
                                    Text(text = "$emoji${pair.first}", color = TextDark, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${pair.second} pts", color = CutePink, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRankingModal = false }) {
                        Text("CERRAR 🎀", color = CutePink, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        // 4. MODAL DE AYUDA
        if (showHelpModal) {
            AlertDialog(
                onDismissRequest = { showHelpModal = false },
                containerColor = CuteCream,
                title = { Text("🎀 MISIÓN: CÓDIGO LIMPIO 🎀", color = TextDark, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("OBJETIVO: Elimina los BUGS y ERRORES con tu espada mágica.", color = TextDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• COMBO: Corta 3+ ítems a la vez. 🎉", color = TextDark.copy(0.8f), fontSize = 13.sp)
                        Text("• IPN CARD: Te regala +5 segundos. 💳", color = TextDark.copy(0.8f), fontSize = 13.sp)
                        Text("• CAFÉ TACHADO: ¡Quita vidas! Evítalo. ☕❌", color = TextDark.copy(0.8f), fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpModal = false }) {
                        Text("¡ENTENDIDO! ✨", color = CutePink, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun ModeSelectorRow(
    selected: FruitNinjaDifficulty,
    onSelected: (FruitNinjaDifficulty) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FruitNinjaDifficulty.entries.forEach { mode ->
            val isSelected = selected == mode
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(mode) },
                label = {
                    Text(
                        text = when(mode) {
                            FruitNinjaDifficulty.CLASSIC -> "🍭 " + mode.label
                            FruitNinjaDifficulty.SAVE_SEMESTER -> "📚 " + mode.label
                            FruitNinjaDifficulty.RELAX -> "☕ " + mode.label
                        },
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = TextDark
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CutePink,
                    selectedLabelColor = TextDark,
                    containerColor = CuteLavender.copy(alpha = 0.4f),
                    labelColor = TextDark.copy(alpha = 0.8f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White,
                    selectedBorderColor = CutePink,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}