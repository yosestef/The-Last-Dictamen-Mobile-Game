package com.android.mobile.games.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.ui.theme.*

private val NeonCyan   = Color(0xFF00F5FF)
private val NeonPurple = Color(0xFFBF00FF)
private val DarkBg     = Color(0xFF0D0221)
private val DarkCard   = Color(0xFF1B065E)

@Composable
fun MainMenuScreen(
    onCodeSlasherClick: () -> Unit,
    onLaRazaRunClick: () -> Unit,
    onCatchGameClick: () -> Unit,
    onCodeMergeClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DarkBg.copy(alpha = 0.92f),
                            DarkBg.copy(alpha = 0.75f),
                            DarkBg.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "THE LAST DICTAMEN",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = NeonCyan.copy(alpha = 0.65f),
                        offset = Offset(0f, 0f),
                        blurRadius = 18f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Minijuegos del Politécnico",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
            )

            GameCard(
                title = "CODE SLASHER",
                emoji = "🗡️",
                description = "Corta los bugs y errores con tu espada",
                accentColor = CutePink,
                onClick = onCodeSlasherClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            GameCard(
                title = "LA RAZA RUN",
                emoji = "🏃",
                iconRes = R.drawable.raza_correr5,
                description = "Corre por el Transbordo de la Ciencia antes de que el tiempo se agote",
                accentColor = Color(0xFFFF5252),
                onClick = onLaRazaRunClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            GameCard(
                title = "THE LAST DICTAMEN",
                emoji = "📜",
                iconRes = R.drawable.ets,
                description = "Sobrevive las 18 semanas en la ESCOM recolectando café y útiles",
                accentColor = Color(0xFFFFD600),
                onClick = onCatchGameClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            GameCard(
                title = "CODE MERGE",
                emoji = "💻",
                iconRes = R.drawable.proyecto_compilado,
                description = "Fusiona el código hasta compilar tu proyecto final",
                accentColor = NeonCyan,
                onClick = onCodeMergeClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    emoji: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit,
    iconRes: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.88f)
        ),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(emoji, fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    fontSize = 14.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "▶",
                color = accentColor,
                fontSize = 16.sp
            )
        }
    }
}
