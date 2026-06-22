package com.android.mobile.games.app.games.fruitninja.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.ui.theme.*

@Composable
fun FruitNinjaGameOverPanel(
    score: Int,
    bestScore: Int,
    difficulty: FruitNinjaDifficulty,
    onRestartClick: () -> Unit,
    onBackToMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.86f),
        color = CuteCream,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(3.dp, CutePink),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧸 GAME OVER 🧸",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )

            Text(
                text = "Modo: ${difficulty.label}",
                color = TextDark.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Puntaje: $score ⭐",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Récord: $bestScore 👑",
                color = TextDark.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Button(
                onClick = onRestartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CutePink,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "¡Intentar de nuevo! ✨", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBackToMenuClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(48.dp),
                border = BorderStroke(2.dp, CuteLavender),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Ir al menú Principal 🎀", fontWeight = FontWeight.Bold)
            }
        }
    }
}