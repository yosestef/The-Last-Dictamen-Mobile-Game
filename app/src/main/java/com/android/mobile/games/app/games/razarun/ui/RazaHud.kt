package com.android.mobile.games.app.games.razarun.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RazaHud(
    distance: Float,
    timeRemaining: Long
) {
    val distanceToGoal = (600f - distance).coerceAtLeast(0f)
    val seconds = (timeRemaining / 1000) % 60
    val minutes = (timeRemaining / 1000) / 60
    val timeStr = String.format("%02d:%02d:00", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Recorrido: ${String.format("%.1f", distance)}m",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp
        )

        Text(
            text = "Distancia a Meta: ${String.format("%.1f", distanceToGoal)}m",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        
        Text(
            text = timeStr,
            color = if (timeRemaining < 10000) Color.Red else Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
