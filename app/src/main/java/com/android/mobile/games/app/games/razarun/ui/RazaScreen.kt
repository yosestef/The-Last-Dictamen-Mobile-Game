package com.android.mobile.games.app.games.razarun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.android.mobile.games.app.games.razarun.engine.RazaGameEngine
import com.android.mobile.games.app.ui.util.HideSystemBars

@Composable
fun RazaScreen(
    onBackToMenuClick: () -> Unit
) {
    HideSystemBars()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { RazaGameEngine(scope) }
    val gameState by engine.gameState

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.y < -50) {
                        engine.jump()
                    } else if (dragAmount.y > 50) {
                        engine.slide()
                    }
                }
            }
    ) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }

        LaunchedEffect(screenWidthPx) {
            engine.setScreenSize(screenWidthPx)
            engine.startGame()
        }
        RazaCanvas(state = gameState)
        
        RazaHud(
            distance = gameState.distance,
            timeRemaining = gameState.timeRemaining
        )

        if (gameState.isGameOver) {
            GameOverOverlay(
                reason = if (gameState.timeRemaining <= 0) "TIME_OUT" else "CRASHED",
                onRetry = { engine.startGame() },
                onExit = onBackToMenuClick
            )
        }

        if (gameState.isVictory) {
            VictoryOverlay(
                onExit = onBackToMenuClick
            )
        }
    }
}

@Composable
fun GameOverOverlay(
    reason: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡GAME OVER!",
                color = Color.Red,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            val message = if (reason == "TIME_OUT") {
                "El profesor cerró la puerta.\nNos vemos en el recursamiento."
            } else {
                "¡Chocaste!"
            }
            
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Button(onClick = onRetry) {
                Text("Reintentar")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(onClick = onExit) {
                Text("Salir")
            }
        }
    }
}

@Composable
fun VictoryOverlay(
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Llegaste a tiempo!",
                color = Color.Green,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "¡Examen Llegado a Tiempo!",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Button(onClick = onExit) {
                Text("Continuar")
            }
        }
    }
}
