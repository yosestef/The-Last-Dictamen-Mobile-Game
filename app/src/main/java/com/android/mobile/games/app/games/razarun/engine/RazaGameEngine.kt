package com.android.mobile.games.app.games.razarun.engine

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.android.mobile.games.app.games.razarun.model.*
import kotlinx.coroutines.*
import kotlin.random.Random

class RazaGameEngine(
    private val scope: CoroutineScope
) {
    private val _gameState = mutableStateOf(RazaGameState())
    val gameState: State<RazaGameState> = _gameState

    private var lastFrameTime = 0L
    private var gameJob: Job? = null
    
    private val GOAL_DISTANCE = 600f
    private val INITIAL_SPEED = 5.0f
    private val SPEED_INCREMENT_INTERVAL = 100f
    private val SPEED_INCREMENT_PERCENT = 0.03f
    private var screenWidth = 1280f

    fun setScreenSize(width: Float) {
        if (width > 0f) screenWidth = width
    }

    fun startGame() {
        _gameState.value = RazaGameState(currentSpeed = INITIAL_SPEED)
        lastFrameTime = System.currentTimeMillis()
        gameJob?.cancel()
        gameJob = scope.launch {
            while (isActive && !_gameState.value.isGameOver && !_gameState.value.isVictory) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastFrameTime) / 1000f
                lastFrameTime = currentTime
                update(deltaTime)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun update(deltaTime: Float) {
        val currentState = _gameState.value
        
        // Update distance
        val newDistance = currentState.distance + currentState.currentSpeed * deltaTime
        
        // Update speed every 100m
        val speedMultiplier = 1.0f + (newDistance / SPEED_INCREMENT_INTERVAL).toInt() * SPEED_INCREMENT_PERCENT
        val newSpeed = INITIAL_SPEED * speedMultiplier

        // Update timer
        val newTimeRemaining = currentState.timeRemaining - (deltaTime * 1000).toLong()
        
        if (newTimeRemaining <= 0) {
            _gameState.value = currentState.copy(
                distance = newDistance,
                timeRemaining = 0,
                isGameOver = true,
                playerAction = RazaPlayerAction.CRASH
            )
            return
        }

        if (newDistance >= GOAL_DISTANCE) {
            _gameState.value = currentState.copy(
                distance = GOAL_DISTANCE,
                isVictory = true,
                playerAction = RazaPlayerAction.WIN
            )
            return
        }

        // Update animation frame
        val frameRate = 0.1f // 100ms per frame
        val newAnimationFrame = (newDistance / (currentState.currentSpeed * frameRate)).toInt()

        // Update obstacles
        // 1. Mover obstáculos existentes
        val updatedObstacles = currentState.obstacles.map {
            it.copy(x = it.x - currentState.currentSpeed * deltaTime * 100)
        }.filter { it.x + it.width > -100f } // Los eliminamos un poco después de salir

        // 2. LÓGICA DE SPAWN DINÁMICO (Aquí va lo que pediste)

        // Calculamos el progreso del juego (de 0.0 a 1.0)
        val progress = (newDistance / GOAL_DISTANCE).coerceIn(0f, 1f)

        // Probabilidad: inicia en 1.5% y sube hasta 4% según el progreso
        val spawnProbability = 0.015f + (progress * 0.025f)

        // Separación mínima: al principio pedimos 800px de espacio, al final solo 400px
        val minDistanceBetween = 800f - (progress * 400f)

        // Verificamos dónde está el último obstáculo creado
        val lastObstacleX = updatedObstacles.maxOfOrNull { it.x } ?: -1f

        // Condición para crear uno nuevo:
        // - Que haya espacio suficiente (minDistanceBetween)
        // - Máximo 3 en pantalla
        // - Que la suerte (Random) lo decida
        val finalObstacles = if (lastObstacleX < (screenWidth - minDistanceBetween) &&
            updatedObstacles.size < 3 &&
            Random.nextFloat() < spawnProbability) {

            val type = RazaObstacleType.entries.random()

            // AJUSTE DE ALTURAS:
            // Carrito: 280f (obstáculo alto para deslizarse)
            // Otros: 350f (obstáculo bajo para saltar)
            val obsY = if (type == RazaObstacleType.CARRETO) 310f else 370f

            updatedObstacles + RazaObstacle(
                x = screenWidth + 100f, // Aparece un poco fuera de la pantalla por la derecha
                y = obsY,
                type = type
            )
        } else {
            updatedObstacles
        }

        /* val updatedObstacles = currentState.obstacles.map {
             it.copy(x = it.x - currentState.currentSpeed * deltaTime * 100) // 100 pixels per meter roughly
         }.filter { it.x + it.width > -200f } // Give more buffer for off-screen removal

         // Spawn obstacles with dynamic distance and probability
         val progress = currentState.distance / GOAL_DISTANCE
         val minDistance = 800f - (progress * 500f) // From 800 to 300
         val spawnChance = 0.02f + (progress * 0.08f) // From 2% to 10%
        
        val lastObsX = updatedObstacles.maxOfOrNull { it.x } ?: -1f
        
        val finalObstacles = if (updatedObstacles.size < 4 && (lastObsX < (1000f - minDistance)) && Random.nextFloat() < spawnChance) {
            val type = RazaObstacleType.entries.random()
            // CARRETO (Carrito de dulces) is a bit higher: must slide under it
            // CHARCO and MOCHILA are at ground level: must jump over them
            // Ground level is roughly 430f (bottom). Obstacles are 100f high.
            val obsY = if (type == RazaObstacleType.CARRETO) 280f else 350f // Nivel de obstaculos
            updatedObstacles + RazaObstacle(
                x = 1000f, // Start off-screen
                y = obsY,
                type = type
            )
        } else {
            updatedObstacles
        }*/

        // Check collisions
        val hasCollision = checkCollisions(currentState.playerAction, finalObstacles)

        if (hasCollision) {
            _gameState.value = currentState.copy(
                distance = newDistance,
                isGameOver = true,
                playerAction = RazaPlayerAction.CRASH
            )
        } else {
            _gameState.value = currentState.copy(
                distance = newDistance,
                currentSpeed = newSpeed,
                timeRemaining = newTimeRemaining,
                animationFrame = newAnimationFrame,
                obstacles = finalObstacles
            )
        }
    }

    private fun checkCollisions(playerAction: RazaPlayerAction, obstacles: List<RazaObstacle>): Boolean {
        val playerX = 150f // Player is a bit further in
        val playerBaseY = 350f
        val playerWidth = 60f // Slightly narrower hit box
        
        // Collision box for player
        val (playerY, playerHeight) = when (playerAction) {
            RazaPlayerAction.JUMP -> 260f to 80f
            RazaPlayerAction.SLIDE -> 410f to 40f
            else -> 350f to 80f
        }

        return obstacles.any { obs ->
            val obsX = obs.x + 20f // Margin for obstacle image
            val obsY = obs.y + 20f
            val obsWidth = obs.width - 40f
            val obsHeight = obs.height - 20f

            val collisionX = playerX < obsX + obsWidth && playerX + playerWidth > obsX
            val collisionY = playerY < obsY + obsHeight && playerY + playerHeight > obsY

            collisionX && collisionY
        }
    }

    fun jump() {
        if (_gameState.value.playerAction == RazaPlayerAction.RUN) {
            scope.launch {
                _gameState.value = _gameState.value.copy(playerAction = RazaPlayerAction.JUMP)
                delay(800) // Jump duration
                if (_gameState.value.playerAction == RazaPlayerAction.JUMP) {
                    _gameState.value = _gameState.value.copy(playerAction = RazaPlayerAction.RUN)
                }
            }
        }
    }

    fun slide() {
        if (_gameState.value.playerAction == RazaPlayerAction.RUN) {
            scope.launch {
                _gameState.value = _gameState.value.copy(playerAction = RazaPlayerAction.SLIDE)
                delay(800) // Slide duration
                if (_gameState.value.playerAction == RazaPlayerAction.SLIDE) {
                    _gameState.value = _gameState.value.copy(playerAction = RazaPlayerAction.RUN)
                }
            }
        }
    }
}
