package com.android.mobile.games.app.games.fruitninja.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.android.mobile.games.app.games.fruitninja.data.FruitNinjaScoreRepository
import com.android.mobile.games.app.games.fruitninja.engine.FruitNinjaGameEngine
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.android.mobile.games.app.games.fruitninja.data.RetrofitGameService
import com.android.mobile.games.app.ui.util.HideSystemBars

@Composable
fun FruitNinjaScreen(
    difficulty: FruitNinjaDifficulty,
    username: String,
    onBackToMenuClick: () -> Unit
) {
    HideSystemBars()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scoreRepository = remember {
        FruitNinjaScoreRepository(context = context)
    }

    val gameService = remember {
        RetrofitGameService()
    }

    val bestScore by scoreRepository
        .getBestScore(difficulty)
        .collectAsState(initial = 0)

    val gameEngine = remember(difficulty) {
        FruitNinjaGameEngine(difficulty = difficulty)
    }

    var gameState by remember(difficulty) {
        mutableStateOf(gameEngine.createInitialState())
    }

    var screenWidth by remember {
        mutableFloatStateOf(0f)
    }

    var screenHeight by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(
        screenWidth,
        screenHeight,
        gameState.isGameOver,
        difficulty
    ) {
        while (
            isActive &&
            screenWidth > 0f &&
            screenHeight > 0f &&
            !gameState.isGameOver
        ) {
            gameState = gameEngine.updateFrame(
                state = gameState,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            delay(16L)
        }
    }

    LaunchedEffect(
        gameState.isGameOver,
        difficulty
    ) {
        var nextTickMs = System.currentTimeMillis() + 1_000L
        while (!gameState.isGameOver && isActive) {
            val toWait = nextTickMs - System.currentTimeMillis()
            if (toWait > 0L) delay(toWait) else delay(1L)
            nextTickMs += 1_000L
            if (!gameState.isGameOver) {
                gameState = gameEngine.updateTimer(state = gameState)
            }
        }
    }

    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            // Guardamos localmente y en el servidor una sola vez al terminar
            scoreRepository.saveBestScoreIfNeeded(
                difficulty = difficulty,
                score = gameState.score
            )
            gameService.uploadScore(
                username = username,
                score = gameState.score,
                difficulty = difficulty.name
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FruitNinjaCanvas(
            gameState = gameState,
            onCanvasSizeChanged = { width, height ->
                screenWidth = width
                screenHeight = height
            },
            onSlice = { touchPoint ->
                gameState = gameEngine.sliceAt(
                    state = gameState,
                    touchPoint = touchPoint
                )
            }
        )

        FruitNinjaHud(
            score = gameState.score,
            bestScore = bestScore,
            lives = gameState.lives,
            timeRemainingSeconds = gameState.timeRemainingSeconds,
            difficulty = difficulty,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (gameState.isGameOver) {
            FruitNinjaGameOverPanel(
                score = gameState.score,
                bestScore = maxOf(bestScore, gameState.score),
                difficulty = difficulty,
                onRestartClick = {
                    gameState = gameEngine.createInitialState()
                },
                onBackToMenuClick = {
                    onBackToMenuClick()
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}