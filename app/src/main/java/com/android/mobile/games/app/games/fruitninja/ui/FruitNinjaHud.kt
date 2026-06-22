package com.android.mobile.games.app.games.fruitninja.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.ui.theme.*

@Composable
fun FruitNinjaHud(
    score: Int,
    bestScore: Int,
    lives: Int,
    timeRemainingSeconds: Int,
    difficulty: FruitNinjaDifficulty,
    modifier: Modifier = Modifier
) {
    val hudTextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = TextDark,
        shadow = Shadow(color = CutePink.copy(alpha = 0.8f), blurRadius = 4f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CuteCream.copy(alpha = 0.9f))
            .border(2.dp, CutePink, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "⭐ SCORE: ${score.toString().padStart(6, '0')}", style = hudTextStyle)
                Text(
                    text = "👑 BEST: ${bestScore.toString().padStart(6, '0')}",
                    style = hudTextStyle.copy(fontSize = 12.sp, shadow = null),
                    color = TextDark.copy(alpha = 0.7f)
                )
            }

            // Vasos de Café (Vidas) - super cute spacing
            if (difficulty.hasLives) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { i ->
                        Image(
                            painter = painterResource(if (i < lives) R.drawable.cafe_con_vida else R.drawable.cafe_menos_vida),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }
            }
        }

        if (difficulty.hasTimer) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { timeRemainingSeconds / 60f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CutePink,
                trackColor = CuteLavender.copy(alpha = 0.3f)
            )
        }
    }
}