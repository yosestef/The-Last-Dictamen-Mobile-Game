package com.android.mobile.games.app.games.catchgame.ui

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
import com.android.mobile.games.app.ui.theme.*

@Composable
fun CatchGameOverPanel(
    score: Int,
    bestScore: Int,
    onRestartClick: () -> Unit,
    onBackToMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(0.86f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(3.dp, CutePink),
        colors = CardDefaults.cardColors(containerColor = CuteCream)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚 SEMESTRE PERDIDO 📚",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            Text(
                text = "Promedio: $score ⭐",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Récord: $bestScore 👑",
                color = TextDark.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRestartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CutePink,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "✨ ¡Reinscripción! ✨", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBackToMenuClick,
                border = BorderStroke(2.dp, CuteLavender),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Ir a Dirección 🎓", fontWeight = FontWeight.Bold)
            }
        }
    }
}
