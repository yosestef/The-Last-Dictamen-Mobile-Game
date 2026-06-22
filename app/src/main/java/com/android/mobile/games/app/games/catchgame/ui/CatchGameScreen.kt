package com.android.mobile.games.app.games.catchgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.catchgame.data.CatchGameScoreRepository
import com.android.mobile.games.app.games.catchgame.data.TriviaJsonLoader
import com.android.mobile.games.app.games.catchgame.data.TriviaRepository
import com.android.mobile.games.app.games.catchgame.engine.CatchGameController
import com.android.mobile.games.app.games.catchgame.data.ICatchGameService
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.android.mobile.games.app.games.catchgame.model.TriviaQuestion
import com.android.mobile.games.app.ui.util.HideSystemBars

@Composable
fun CatchGameScreen(
    difficulty: CatchGameDifficulty,
    username: String,
    gameService: ICatchGameService,
    onBackToMenuClick: () -> Unit
) {
    HideSystemBars()
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val scoreRepository = remember {
        CatchGameScoreRepository(context = context)
    }

    val bestScore by scoreRepository
        .getBestScore(difficulty)
        .collectAsState(initial = 0)

    var questions by    remember {
        mutableStateOf<List<TriviaQuestion>>(emptyList())
    }

    LaunchedEffect(Unit) {
        questions = withContext(Dispatchers.IO) {
            TriviaJsonLoader.loadQuestions(context)
        }
    }

    val triviaRepository = remember(questions) {
        TriviaRepository(questions)
    }

    var sessionId by remember {
        mutableIntStateOf(0)
    }

    val controller = remember(difficulty, sessionId, questions) {
        if (questions.isNotEmpty()) {
            triviaRepository.resetSession()
            CatchGameController(
                difficulty = difficulty,
                triviaRepository = triviaRepository
            )
        } else {
            null
        }
    }

    if (controller == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             androidx.compose.material3.CircularProgressIndicator(color = com.android.mobile.games.app.ui.theme.CutePink)
        }
        return
    }

    val uiState = controller.uiState

    LaunchedEffect(controller) {
        var lastFrameTimeNanos = 0L

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    controller.update(deltaSeconds)
                }

                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    LaunchedEffect(
        controller,
        uiState.isTriviaVisible,
        uiState.isTriviaAnswerLocked
    ) {
        while (
            isActive &&
            uiState.isTriviaVisible &&
            !uiState.isGameOver &&
            !uiState.isTriviaAnswerLocked
        ) {
            delay(1_000L)
            controller.tickTriviaTimer()
        }
    }

    LaunchedEffect(
        controller,
        uiState.triviaFeedbackMessage,
        uiState.isTriviaAnswerLocked
    ) {
        if (
            uiState.isTriviaVisible &&
            uiState.isTriviaAnswerLocked &&
            uiState.triviaFeedbackMessage != null
        ) {
            delay(1_200L)
            controller.resolveTriviaAfterFeedback()
        }
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            // 1. Guardado local — sigue funcionando igual que antes
            scoreRepository.saveBestScoreIfNeeded(
                difficulty = difficulty,
                score = uiState.score
            )
            // 2. Envío al servidor — fallo silencioso si no hay red
            if (username.isNotBlank()) {
                gameService.submitScore(
                    username = username,
                    score = uiState.score,
                    difficulty = difficulty.name
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(controller, uiState.isTriviaVisible) {
                if (!uiState.isTriviaVisible) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            controller.setPlayerTargetByTouch(offset.x)
                        },
                        onDrag = { change, _ ->
                            controller.setPlayerTargetByTouch(change.position.x)
                        }
                    )
                }
            }
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val playerWidthPx = (screenWidthPx * 0.40f).coerceIn(200f, 500f)
        val playerHeightPx = playerWidthPx
        val itemSizePx = (screenWidthPx * 0.25f).coerceIn(100f, 150f)
        val floorMarginPx = (screenHeightPx * 0.022f).coerceIn(12f, 26f)
        val playerSizeDp = with(density) { playerWidthPx.toDp() }
        val itemSizeDp = with(density) { itemSizePx.toDp() }

        LaunchedEffect(
            controller,
            screenWidthPx,
            screenHeightPx,
            playerWidthPx,
            itemSizePx
        ) {
            controller.initializeLayout(
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                playerWidthPx = playerWidthPx,
                playerHeightPx = playerHeightPx,
                itemSizePx = itemSizePx,
                floorMarginPx = floorMarginPx
            )
        }

        Image(
            painter = painterResource(id = R.drawable.bg_game),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        uiState.items.forEach { item ->
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = item.x.roundToInt(),
                            y = item.y.roundToInt()
                        )
                    }
                    .size(itemSizeDp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.player_character),
            contentDescription = null,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = uiState.playerX.roundToInt(),
                        y = uiState.playerY.roundToInt()
                    )
                }
                .size(playerSizeDp)
        )

        CatchGameHud(
            score = uiState.score,
            bestScore = maxOf(bestScore, uiState.score),
            lives = uiState.lives,
            missedCount = uiState.missedCount,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        uiState.activeTriviaQuestion?.let { question ->
            if (uiState.isTriviaVisible) {
                CatchTriviaDialog(
                    question = question,
                    timeLeftSeconds = uiState.triviaTimeLeftSeconds,
                    feedbackMessage = uiState.triviaFeedbackMessage,
                    isAnswerLocked = uiState.isTriviaAnswerLocked,
                    onAnswerSelected = { selectedIndex ->
                        controller.answerTrivia(selectedIndex)
                    }
                )
            }
        }

        if (uiState.isGameOver) {
            CatchGameOverPanel(
                score = uiState.score,
                bestScore = maxOf(bestScore, uiState.score),
                onRestartClick = {
                    coroutineScope.launch {
                        scoreRepository.saveBestScoreIfNeeded(
                            difficulty = difficulty,
                            score = uiState.score
                        )

                        sessionId += 1
                    }
                },
                onBackToMenuClick = {
                    coroutineScope.launch {
                        scoreRepository.saveBestScoreIfNeeded(
                            difficulty = difficulty,
                            score = uiState.score
                        )

                        onBackToMenuClick()
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
