package com.android.mobile.games.app.games.razarun.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.ui.platform.LocalContext
import com.android.mobile.games.app.R
import com.android.mobile.games.app.ui.util.HideSystemBars

@Composable
fun RazaMenuScreen(
    onStartGameClick: () -> Unit,
    onBackClick: () -> Unit
) {
    HideSystemBars()
    var showHelp by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Portada
        Image(
            painter = painterResource(id = com.android.mobile.games.app.R.drawable.raza_portada),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Help Button (Top Right)
        IconButton(
            onClick = { showHelp = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        // Back Button (Top Left)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        // Start Button (Bottom)
        Button(
            onClick = onStartGameClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .height(60.dp)
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                text = "A correr",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (showHelp) {
            HelpDialog(onDismiss = { showHelp = false })
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cómo jugar?") },
        text = {
            Column {
                Text("Desliza hacia ARRIBA para SALTAR obstáculos bajos (mochilas, charcos).")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Desliza hacia ABAJO para DESLIZARTE bajo obstáculos altos (carritos).")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Llega a los 600m antes de que se acabe el tiempo (90s).")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}
