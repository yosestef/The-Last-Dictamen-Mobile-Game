package com.android.mobile.games.app.games.catchgame.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.mobile.games.app.games.catchgame.config.CatchGameConfig
import com.android.mobile.games.app.games.catchgame.data.TriviaRepository
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import com.android.mobile.games.app.games.catchgame.model.CatchGameItem
import com.android.mobile.games.app.games.catchgame.model.CatchGameUiState
import kotlin.math.max
import kotlin.random.Random

class CatchGameController(
    private val difficulty: CatchGameDifficulty,
    private val triviaRepository: TriviaRepository
) {

    var uiState by mutableStateOf(
        CatchGameUiState(lives = CatchGameConfig.INITIAL_LIVES)
    )
        private set

    private val random = Random(System.currentTimeMillis())

    private var screenWidthPx = 0f
    private var screenHeightPx = 0f
    private var playerWidthPx = 0f
    private var playerHeightPx = 0f
    private var itemSizePx = 0f
    private var floorMarginPx = 0f

    private var isInitialized = false
    private var targetPlayerX = 0f
    private var spawnAccumulatorMs = 0f
    private var totalElapsedTimeSeconds = 0f
    private var nextItemId = 0L

    fun initializeLayout(
        screenWidthPx: Float,
        screenHeightPx: Float,
        playerWidthPx: Float,
        playerHeightPx: Float,
        itemSizePx: Float,
        floorMarginPx: Float
    ) {
        if (
            isInitialized &&
            this.screenWidthPx == screenWidthPx &&
            this.screenHeightPx == screenHeightPx
        ) {
            return
        }

        this.screenWidthPx = screenWidthPx
        this.screenHeightPx = screenHeightPx
        this.playerWidthPx = playerWidthPx
        this.playerHeightPx = playerHeightPx
        this.itemSizePx = itemSizePx
        this.floorMarginPx = floorMarginPx

        val initialPlayerX = (screenWidthPx - playerWidthPx) / 2f
        val initialPlayerY = screenHeightPx - playerHeightPx - floorMarginPx

        targetPlayerX = if (isInitialized) {
            targetPlayerX.coerceIn(-playerWidthPx / 2f, screenWidthPx - playerWidthPx / 2f)
        } else {
            initialPlayerX
        }

        uiState = uiState.copy(
            playerX = if (isInitialized) {
                uiState.playerX.coerceIn(-playerWidthPx / 2f, screenWidthPx - playerWidthPx / 2f)
            } else {
                initialPlayerX
            },
            playerY = initialPlayerY
        )

        isInitialized = true
    }

    fun setPlayerTargetByTouch(touchX: Float) {
        if (!isInitialized || uiState.isGameOver || uiState.isTriviaVisible) {
            return
        }

        targetPlayerX = (touchX - playerWidthPx / 2f)
            .coerceIn(-playerWidthPx / 2f, screenWidthPx - playerWidthPx / 2f)
    }

    fun update(deltaSeconds: Float) {
        if (!isInitialized || uiState.isGameOver || uiState.isTriviaVisible) {
            return
        }

        totalElapsedTimeSeconds += deltaSeconds

        updatePlayer(deltaSeconds)
        updateSpawner(deltaSeconds)
        updateItems(deltaSeconds)
    }

    fun answerTrivia(selectedIndex: Int) {
        val question = uiState.activeTriviaQuestion ?: return

        if (uiState.isTriviaAnswerLocked) {
            return
        }

        val isCorrect = selectedIndex == question.correctAnswerIndex

        uiState = uiState.copy(
            triviaFeedbackMessage = if (isCorrect) {
                "Correct. You kept your final life."
            } else {
                "Incorrect. Game over."
            },
            isTriviaAnswerLocked = true,
            triviaAnswerWasCorrect = isCorrect
        )
    }

    fun resolveTriviaAfterFeedback() {
        val wasCorrect = uiState.triviaAnswerWasCorrect ?: return

        uiState = if (wasCorrect) {
            uiState.copy(
                activeTriviaQuestion = null,
                isTriviaVisible = false,
                triviaTimeLeftSeconds = 0,
                triviaFeedbackMessage = null,
                isTriviaAnswerLocked = false,
                triviaAnswerWasCorrect = null
            )
        } else {
            uiState.copy(
                activeTriviaQuestion = null,
                isTriviaVisible = false,
                triviaTimeLeftSeconds = 0,
                triviaFeedbackMessage = null,
                isTriviaAnswerLocked = false,
                triviaAnswerWasCorrect = null,
                isGameOver = true
            )
        }
    }

    fun tickTriviaTimer() {
        if (
            !uiState.isTriviaVisible ||
            uiState.activeTriviaQuestion == null ||
            uiState.isGameOver ||
            uiState.isTriviaAnswerLocked
        ) {
            return
        }

        val updatedTime = uiState.triviaTimeLeftSeconds - 1

        uiState = if (updatedTime <= 0) {
            uiState.copy(
                triviaTimeLeftSeconds = 0,
                triviaFeedbackMessage = "Time is up. Game over.",
                isTriviaAnswerLocked = true,
                triviaAnswerWasCorrect = false
            )
        } else {
            uiState.copy(triviaTimeLeftSeconds = updatedTime)
        }
    }

    private fun updatePlayer(deltaSeconds: Float) {
        val updatedX = uiState.playerX +
                (targetPlayerX - uiState.playerX) *
                CatchGameConfig.PLAYER_SMOOTHING *
                deltaSeconds

        uiState = uiState.copy(
            playerX = updatedX.coerceIn(-playerWidthPx / 2f, screenWidthPx - playerWidthPx / 2f)
        )
    }

    private fun updateSpawner(deltaSeconds: Float) {
        spawnAccumulatorMs += deltaSeconds * 1000f

        val timeLevel = (totalElapsedTimeSeconds / 10f).toInt()
        val spawnMultiplier = kotlin.math.max(0.3f, 1f - timeLevel * 0.10f)
        val currentSpawnIntervalMs = difficulty.spawnIntervalMs * spawnMultiplier

        while (spawnAccumulatorMs >= currentSpawnIntervalMs) {
            spawnAccumulatorMs -= currentSpawnIntervalMs
            spawnItem()
        }
    }

    private fun spawnItem() {
        val timeLevel = (totalElapsedTimeSeconds / 10f).toInt()
        val speedMultiplier = 1f + timeLevel * 0.15f

        val isBad = random.nextFloat() < difficulty.badObjectProbability
        val drawableRes = if (isBad) {
            CatchGameConfig.badDrawables.random(random)
        } else {
            CatchGameConfig.foodDrawables.random(random)
        }
        val speed = screenHeightPx * (
                difficulty.minFallSpeedHeightRatio +
                        random.nextFloat() *
                        (difficulty.maxFallSpeedHeightRatio - difficulty.minFallSpeedHeightRatio)
                ) * speedMultiplier
        val maxX = kotlin.math.max(0f, screenWidthPx - itemSizePx)

        uiState = uiState.copy(
            items = uiState.items + CatchGameItem(
                id = nextItemId++,
                x = random.nextFloat() * maxX,
                y = -itemSizePx,
                speedPxPerSecond = speed,
                drawableRes = drawableRes,
                isBad = isBad
            )
        )
    }

    private fun updateItems(deltaSeconds: Float) {
        val updatedItems = mutableListOf<CatchGameItem>()
        var score = uiState.score
        var lives = uiState.lives
        var gameOver = uiState.isGameOver
        var triggerTrivia = false
        var rescueChanceUsed = uiState.rescueChanceUsed
        var missedCount = uiState.missedCount

        val missedLimit = when (difficulty) {
            CatchGameDifficulty.EASY -> 7
            CatchGameDifficulty.MEDIUM -> 5
            CatchGameDifficulty.HARD -> 3
        }

        uiState.items.forEach { item ->
            val movedItem = item.copy(
                y = item.y + item.speedPxPerSecond * deltaSeconds
            )

            if (isCollidingWithPlayer(movedItem)) {
                if (movedItem.isBad) {
                    when {
                        lives > 1 -> lives -= 1
                        lives == 1 && !rescueChanceUsed -> {
                            triggerTrivia = true
                            rescueChanceUsed = true
                        }
                        else -> gameOver = true
                    }
                } else {
                    score += 1
                }
                return@forEach
            }

            if (movedItem.y < screenHeightPx) {
                updatedItems += movedItem
            } else {
                if (!movedItem.isBad) {
                    missedCount += 1
                    if (missedCount > missedLimit) {
                        gameOver = true
                    }
                }
            }
        }

        val nextState = uiState.copy(
            score = score,
            lives = lives,
            items = updatedItems,
            isGameOver = gameOver,
            rescueChanceUsed = rescueChanceUsed,
            missedCount = missedCount
        )

        uiState = if (triggerTrivia) {
            nextState.copy(
                activeTriviaQuestion = triviaRepository.getRandomQuestion(),
                isTriviaVisible = true,
                triviaTimeLeftSeconds = CatchGameConfig.TRIVIA_TIME_SECONDS,
                triviaFeedbackMessage = null,
                isTriviaAnswerLocked = false,
                triviaAnswerWasCorrect = null
            )
        } else {
            nextState
        }
    }

    private fun isCollidingWithPlayer(item: CatchGameItem): Boolean {
        val playerLeft = uiState.playerX
        val playerTop = uiState.playerY
        val playerRight = playerLeft + playerWidthPx
        val playerBottom = playerTop + playerHeightPx

        val itemLeft = item.x
        val itemTop = item.y
        val itemRight = itemLeft + itemSizePx
        val itemBottom = itemTop + itemSizePx

        return itemRight > playerLeft &&
                itemLeft < playerRight &&
                itemBottom > playerTop &&
                itemTop < playerBottom
    }
}
