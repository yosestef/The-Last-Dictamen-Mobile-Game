package com.android.mobile.games.app.games.codemerge.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.codemerge.model.CodeLevel
import com.android.mobile.games.app.games.codemerge.model.CodeMergeGameState
import com.android.mobile.games.app.games.codemerge.model.CodeMergeIntent
import com.android.mobile.games.app.ui.util.HideSystemBars

@Composable
fun CodeMergeScreen(
    state: CodeMergeGameState,
    onIntent: (CodeMergeIntent) -> Unit,
    onBackClick: () -> Unit
) {
    HideSystemBars()
    val nuloImg = ImageBitmap.imageResource(id = R.drawable.nulo)
    val bugImg = ImageBitmap.imageResource(id = R.drawable.bug)
    val errorImg = ImageBitmap.imageResource(id = R.drawable.error)
    val cardImg = ImageBitmap.imageResource(id = R.drawable.ipn_card)
    val cafeImg = ImageBitmap.imageResource(id = R.drawable.cafe_con_vida)
    val projectImg = ImageBitmap.imageResource(id = R.drawable.proyecto_compilado) // Use specific compiled project sprite

    val assetMap = remember(nuloImg, bugImg, errorImg, cardImg, cafeImg, projectImg) {
        mapOf(
            CodeLevel.NULO to nuloImg,
            CodeLevel.BUG to bugImg,
            CodeLevel.ERROR to errorImg,
            CodeLevel.IPN_CARD to cardImg,
            CodeLevel.CAFE to cafeImg,
            CodeLevel.PROJECT_COMPLETE to projectImg
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0221)) // Dark Deep Space
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HUD
            CodeMergeHud(state = state)

            // Game Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                onIntent(CodeMergeIntent.MoveCurrentElement(change.position.x * (1000f / size.width)))
                            },
                            onDragEnd = {
                                onIntent(CodeMergeIntent.DropElement)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                             onIntent(CodeMergeIntent.MoveCurrentElement(offset.x * (1000f / size.width)))
                             onIntent(CodeMergeIntent.DropElement)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / 1000f
                    val scaleY = size.height / 1000f
                    val baseRadius = 30f * scaleX // Base radius 30dp

                    // Loss Line
                    drawLine(
                        color = Color(0xFFBF00FF), // Neon Purple
                        start = Offset(0f, 150f * scaleY),
                        end = Offset(size.width, 150f * scaleY),
                        strokeWidth = 4f,
                        pathEffect = null
                    )

                    // Current Element Preview
                    val currentRadius = baseRadius * state.nextLevel.radiusScale
                    val img = assetMap[state.nextLevel] ?: return@Canvas
                    drawImage(
                        image = img,
                        dstOffset = IntOffset(
                            (state.currentElementX * scaleX - currentRadius).toInt(),
                            (50f * scaleY - currentRadius).toInt()
                        ),
                        dstSize = IntSize((currentRadius * 2).toInt(), (currentRadius * 2).toInt())
                    )

                    // Falling Elements
                    state.elements.forEach { el ->
                        val radius = baseRadius * el.level.radiusScale
                        val elementImg = assetMap[el.level] ?: return@forEach
                        drawImage(
                            image = elementImg,
                            dstOffset = IntOffset(
                                (el.x * scaleX - radius).toInt(),
                                (el.y * scaleY - radius).toInt()
                            ),
                            dstSize = IntSize((radius * 2).toInt(), (radius * 2).toInt())
                        )
                    }
                }
            }
        }

        if (state.isGameOver) {
            GameOverOverlay(score = state.currentScore, onRestart = { onIntent(CodeMergeIntent.StartGame) }, onBack = onBackClick)
        }
        
        if (state.isVictory) {
            VictoryOverlay(score = state.currentScore, onRestart = { onIntent(CodeMergeIntent.StartGame) }, onBack = onBackClick)
        }
    }
}

@Composable
fun VictoryOverlay(score: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("¡VICTORIA!", color = Color(0xFF00F5FF), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡PROYECTO FINAL APROBADO!\nERES UN MASTER DE LA COMPILACIÓN",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Puntaje Final: $score", color = Color(0xFFBF00FF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            androidx.compose.material3.Button(
                onClick = onRestart,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFBF00FF))
            ) {
                Text("VOLVER A JUGAR", color = Color.White)
            }
            androidx.compose.material3.TextButton(onClick = onBack) {
                Text("VOLVER AL MENÚ", color = Color.White)
            }
        }
    }
}

@Composable
fun CodeMergeHud(state: CodeMergeGameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1B065E), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("PUNTAJE", color = Color(0xFF00F5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${state.currentScore}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
             Text("SIGUIENTE", color = Color(0xFFBF00FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
             // In a real implementation, we might show a small preview image here
             Text(state.nextLevel.name, color = Color.White, fontSize = 14.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("RECORD", color = Color(0xFF00F5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${state.highScore}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", color = Color.Red, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
            Text("Puntaje Final: $score", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Button(onClick = onRestart) {
                Text("REINTENTAR")
            }
            androidx.compose.material3.TextButton(onClick = onBack) {
                Text("VOLVER AL MENÚ", color = Color.White)
            }
        }
    }
}
