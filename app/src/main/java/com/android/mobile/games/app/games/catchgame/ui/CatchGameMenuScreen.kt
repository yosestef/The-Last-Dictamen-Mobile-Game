package com.android.mobile.games.app.games.catchgame.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.catchgame.data.CatchGameRankingEntry
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import com.android.mobile.games.app.ui.theme.*
import com.android.mobile.games.app.ui.util.HideSystemBars
import kotlinx.coroutines.launch

@Composable
fun CatchGameMenuScreen(
    selectedDifficulty: CatchGameDifficulty,
    onDifficultySelected: (CatchGameDifficulty) -> Unit,
    onStartGameClick: (username: String) -> Unit,
    onBackClick: () -> Unit,
    rankingLoader: suspend (difficulty: String?, limit: Int) -> List<CatchGameRankingEntry>
) {
    HideSystemBars()
    var username by remember { mutableStateOf("") }
    var showRankingModal by remember { mutableStateOf(false) }
    var rankingData by remember { mutableStateOf<List<CatchGameRankingEntry>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CutePink.copy(alpha = 0.15f))
        )

        // Botón Ranking (top-right)
        IconButton(
            onClick = {
                showRankingModal = true
                isLoadingRanking = true
                coroutineScope.launch {
                    rankingData = rankingLoader(selectedDifficulty.name, 10)
                    isLoadingRanking = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(52.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CuteYellow,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("🏆", fontSize = 22.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CuteCream.copy(alpha = 0.9f),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(3.dp, CuteLavender),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📜 THE LAST DICTAMEN ⚖️",
                        color = TextDark,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Elige tu nivel de sufrimiento académico",
                        color = TextDark.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Campo de nombre del jugador
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Tu nombre de estudiante", color = TextDark.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.EASY,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CuteMint,
                            modifier = Modifier.weight(1f)
                        )
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.MEDIUM,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CuteYellow,
                            modifier = Modifier.weight(1f)
                        )
                        CatchDifficultyCard(
                            difficulty = CatchGameDifficulty.HARD,
                            selectedDifficulty = selectedDifficulty,
                            onClick = onDifficultySelected,
                            color = CutePink,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onStartGameClick(username.trim()) },
                        enabled = username.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CutePink,
                            contentColor = TextDark,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(text = "✨ ¡A clases! ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBackClick,
                        border = BorderStroke(2.dp, CuteLavender),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Salir de la ESCOM 🎓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Modal de Ranking
        if (showRankingModal) {
            AlertDialog(
                onDismissRequest = { showRankingModal = false },
                containerColor = CuteCream,
                title = {
                    Text(
                        "👑 MEJORES DICTÁMENES (${selectedDifficulty.label})",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        if (isLoadingRanking) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = CutePink
                            )
                        } else if (rankingData.isEmpty()) {
                            Text("Sin registros aún. ¡Sé el primero! 📚", color = TextDark)
                        } else {
                            rankingData.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val medal = when (index) {
                                        0 -> "🥇 "
                                        1 -> "🥈 "
                                        2 -> "🥉 "
                                        else -> "${index + 1}. "
                                    }
                                    Text(
                                        text = "$medal${entry.username}",
                                        color = TextDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${entry.score} pts",
                                        color = CutePink,
                                        fontWeight = FontWeight.Bold
                                    )
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
    }
}

@Composable
private fun CatchDifficultyCard(
    difficulty: CatchGameDifficulty,
    selectedDifficulty: CatchGameDifficulty,
    onClick: (CatchGameDifficulty) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isSelected = difficulty == selectedDifficulty

    Card(
        onClick = { onClick(difficulty) },
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) color else color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.4f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val emoji = when (difficulty) {
                CatchGameDifficulty.EASY -> "😊"
                CatchGameDifficulty.MEDIUM -> "😐"
                CatchGameDifficulty.HARD -> "💀"
            }
            Text(emoji, fontSize = 24.sp)
            Text(
                text = difficulty.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}
