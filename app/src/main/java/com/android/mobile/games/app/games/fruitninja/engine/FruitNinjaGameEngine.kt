package com.android.mobile.games.app.games.fruitninja.engine

import androidx.compose.ui.geometry.Offset
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaEffect
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaEffectType
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaGameState
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItem
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItemType
import com.android.mobile.games.app.games.fruitninja.model.isBomb
import com.android.mobile.games.app.games.fruitninja.model.isFruit
import com.android.mobile.games.app.games.fruitninja.model.isPenalty
import com.android.mobile.games.app.games.fruitninja.util.createRandomFruitNinjaItem
import com.android.mobile.games.app.games.fruitninja.util.isPointInsideItem

private const val INITIAL_LIVES = 3
private const val INITIAL_TIME_SECONDS = 60
private const val EFFECT_GRAVITY = 0.38f
private const val SPLASH_MAX_AGE_FRAMES = 24
private const val FRUIT_HALF_MAX_AGE_FRAMES = 70
private const val EXPLOSION_MAX_AGE_FRAMES = 32

class FruitNinjaGameEngine(
    private val difficulty: FruitNinjaDifficulty
) {

    private var nextItemId: Long = 0L
    private var nextEffectId: Long = 0L
    private var frameCounter: Int = 0

    fun createInitialState(): FruitNinjaGameState {
        resetEngine()

        return FruitNinjaGameState(
            items = emptyList(),
            effects = emptyList(),
            score = 0,
            lives = INITIAL_LIVES,
            timeRemainingSeconds = INITIAL_TIME_SECONDS,
            difficulty = difficulty,
            isRunning = true,
            isPaused = false,
            isGameOver = false
        )
    }

    fun updateFrame(
        state: FruitNinjaGameState,
        screenWidth: Float,
        screenHeight: Float
    ): FruitNinjaGameState {
        if (!state.isRunning || state.isPaused || state.isGameOver) {
            return state
        }

        frameCounter++

        val movedItems = moveItems(state.items)

        val visibleItems = getVisibleItems(
            items = movedItems,
            screenHeight = screenHeight
        )

        // 1. Corrigiendo aqui se calcula cuántos ítems se cayeron----- acuerdateee
        val missedFruits = countMissedFruits(
            items = movedItems,
            screenHeight = screenHeight
        )

        // 2. CORRECCIÓN PARA MODO RELAX:
        // Solo restamos vidas si la dificultad TIENE vidas (hasLives == true)
        val updatedLives = if (difficulty.hasLives) {
            (state.lives - missedFruits).coerceAtLeast(0)
        } else {
            state.lives // En Relax, las vidas se mantienen intactas
        }

        val itemsWithSpawn = spawnItemIfNeeded(
            items = visibleItems,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        val updatedEffects = updateEffects(state.effects)

        return state.copy(
            items = itemsWithSpawn,
            effects = updatedEffects,
            lives = updatedLives,
            // Solo es Game Over por vidas si el modo las usa
            isGameOver = (difficulty.hasLives && updatedLives <= 0)
        )
    }

    fun updateTimer(
        state: FruitNinjaGameState
    ): FruitNinjaGameState {
        if (!state.isRunning || state.isPaused || state.isGameOver) {
            return state
        }

        val updatedTime = (state.timeRemainingSeconds - 1).coerceAtLeast(0)

        return state.copy(
            timeRemainingSeconds = updatedTime,
            isGameOver = updatedTime <= 0
        )
    }

    fun sliceAt(state: FruitNinjaGameState, touchPoint: Offset): FruitNinjaGameState {
        if (!state.isRunning || state.isPaused || state.isGameOver) return state

        val touchedItems = state.items.filter { isPointInsideItem(touchPoint, it) }
        if (touchedItems.isEmpty()) return state

        var newLives = state.lives
        var newTime = state.timeRemainingSeconds
        var isGameOver = state.isGameOver
        var newScore = state.score
        var newBugsEliminated = state.bugsEliminated

        touchedItems.forEach { item ->
            if (item.type.isPenalty()) {
                if (state.difficulty == FruitNinjaDifficulty.CLASSIC ||
                    state.difficulty == FruitNinjaDifficulty.SAVE_SEMESTER) isGameOver = true
                else newLives = (newLives - 1).coerceAtLeast(0)
            } else {
                newScore += state.difficulty.pointsPerItem
                if (item.type != FruitNinjaItemType.IPN_CARD) {
                    newBugsEliminated++
                }
                if (item.type == FruitNinjaItemType.IPN_CARD) {
                    newTime += 5 // Bonus +5 segundos
                }
            }
        }

        val remainingItems = state.items.filterNot { it in touchedItems }
        val sliceEffects = touchedItems.flatMap { createSliceEffects(it) }

        return state.copy(
            items = remainingItems,
            effects = state.effects + sliceEffects,
            score = newScore,
            lives = newLives,
            timeRemainingSeconds = newTime,
            bugsEliminated = newBugsEliminated,
            // Par que solo termine si se acaban las vidas cuando el modo no es relax
            isGameOver = isGameOver || (newLives <= 0 && state.difficulty.hasLives)
        )
    }

    private fun resetEngine() {
        nextItemId = 0L
        nextEffectId = 0L
        frameCounter = 0
    }

    private fun moveItems(
        items: List<FruitNinjaItem>
    ): List<FruitNinjaItem> {
        return items.map { item ->
            val updatedVelocity = item.velocity.copy(
                y = item.velocity.y + difficulty.gravity
            )

            val updatedPosition = item.position + updatedVelocity

            item.copy(
                position = updatedPosition,
                velocity = updatedVelocity
            )
        }
    }

    private fun updateEffects(
        effects: List<FruitNinjaEffect>
    ): List<FruitNinjaEffect> {
        return effects
            .map { effect ->
                val updatedVelocity = when (effect.effectType) {
                    FruitNinjaEffectType.HALF_ONE,
                    FruitNinjaEffectType.HALF_TWO -> effect.velocity.copy(
                        y = effect.velocity.y + EFFECT_GRAVITY
                    )

                    FruitNinjaEffectType.SPLASH,
                    FruitNinjaEffectType.EXPLOSION -> effect.velocity
                }

                val updatedPosition = effect.position + updatedVelocity

                effect.copy(
                    position = updatedPosition,
                    velocity = updatedVelocity,
                    ageFrames = effect.ageFrames + 1
                )
            }
            .filter { effect ->
                effect.ageFrames < effect.maxAgeFrames
            }
    }

    private fun getVisibleItems(
        items: List<FruitNinjaItem>,
        screenHeight: Float
    ): List<FruitNinjaItem> {
        return items.filter { item ->
            item.position.y < screenHeight + item.radius * 2
        }
    }

    private fun countMissedFruits(
        items: List<FruitNinjaItem>,
        screenHeight: Float
    ): Int {
        return items.count { item ->
            item.type.isFruit() &&
                    item.position.y >= screenHeight + item.radius * 2
        }
    }

    private fun spawnItemIfNeeded(
        items: List<FruitNinjaItem>,
        screenWidth: Float,
        screenHeight: Float
    ): List<FruitNinjaItem> {
        val canSpawn = items.size < difficulty.maxItemsOnScreen
        val shouldSpawn = frameCounter % difficulty.spawnIntervalFrames == 0

        if (!canSpawn || !shouldSpawn) {
            return items
        }

        val newItem = createRandomFruitNinjaItem(
            id = nextItemId++,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            difficulty = difficulty
        )

        return items + newItem
    }

    private fun createSliceEffects(
        item: FruitNinjaItem
    ): List<FruitNinjaEffect> {
        return listOf(
            createSplashEffect(item),
            createFruitHalfEffect(
                item = item,
                effectType = FruitNinjaEffectType.HALF_ONE,
                velocity = Offset(
                    x = -5.5f,
                    y = -3.5f
                )
            ),
            createFruitHalfEffect(
                item = item,
                effectType = FruitNinjaEffectType.HALF_TWO,
                velocity = Offset(
                    x = 5.5f,
                    y = -3.5f
                )
            )
        )
    }

    private fun createSplashEffect(
        item: FruitNinjaItem
    ): FruitNinjaEffect {
        return FruitNinjaEffect(
            id = nextEffectId++,
            itemType = item.type,
            effectType = FruitNinjaEffectType.SPLASH,
            position = item.position,
            size = item.radius * 3.1f,
            maxAgeFrames = SPLASH_MAX_AGE_FRAMES
        )
    }

    private fun createFruitHalfEffect(
        item: FruitNinjaItem,
        effectType: FruitNinjaEffectType,
        velocity: Offset
    ): FruitNinjaEffect {
        return FruitNinjaEffect(
            id = nextEffectId++,
            itemType = item.type,
            effectType = effectType,
            position = item.position,
            velocity = velocity,
            size = item.radius * 2.5f,
            maxAgeFrames = FRUIT_HALF_MAX_AGE_FRAMES
        )
    }

    private fun createExplosionEffect(
        item: FruitNinjaItem
    ): FruitNinjaEffect {
        return FruitNinjaEffect(
            id = nextEffectId++,
            itemType = item.type,
            effectType = FruitNinjaEffectType.EXPLOSION,
            position = item.position,
            size = item.radius * 3.2f,
            maxAgeFrames = EXPLOSION_MAX_AGE_FRAMES
        )
    }
}

