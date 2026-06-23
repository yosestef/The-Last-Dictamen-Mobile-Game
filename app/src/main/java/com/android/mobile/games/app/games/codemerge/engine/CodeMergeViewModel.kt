package com.android.mobile.games.app.games.codemerge.engine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.mobile.games.app.games.codemerge.data.ICodeMergeGameService
import com.android.mobile.games.app.games.codemerge.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import kotlin.random.Random

class CodeMergeViewModel(
    private val service: ICodeMergeGameService
) : ViewModel() {

    private val _state = MutableStateFlow(CodeMergeGameState())
    val state: StateFlow<CodeMergeGameState> = _state.asStateFlow()

    private val GRAVITY = 0.4f
    private val BOUNCE = 0.1f
    private val FRICTION = 0.95f
    private val BASE_RADIUS = 30f
    private val SLEEP_THRESHOLD = 0.2f
    private val LOSS_LINE_Y = 150f
    private var gameOverTimer = 0L

    init {
        startGameLoop()
    }

    fun handleIntent(intent: CodeMergeIntent) {
        viewModelScope.launch {
            when (intent) {
                is CodeMergeIntent.StartGame -> {
                    _state.update { CodeMergeGameState() }
                    gameOverTimer = 0L
                }
                is CodeMergeIntent.MoveCurrentElement -> {
                    if (!_state.value.isGameOver && !_state.value.isVictory) {
                        _state.update { it.copy(currentElementX = intent.x) }
                    }
                }
                is CodeMergeIntent.DropElement -> {
                    dropElement()
                }
                is CodeMergeIntent.Tick -> {
                    withContext(Dispatchers.Default) {
                        updatePhysics()
                    }
                }
                is CodeMergeIntent.SubmitScore -> {
                    submitScore(intent.name)
                }
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(16) // ~60 FPS
                handleIntent(CodeMergeIntent.Tick)
            }
        }
    }

    private fun getWeightedRandomLevel(): CodeLevel {
        val rand = Random.nextFloat()
        return when {
            rand < 0.60f -> CodeLevel.NULO   // 60%
            rand < 0.90f -> CodeLevel.BUG    // 30%
            else -> CodeLevel.ERROR          // 10%
        }
    }

    private fun dropElement() {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.isVictory) return
        
        val newElement = CodeElement(
            x = currentState.currentElementX,
            y = 50f,
            level = currentState.nextLevel
        )
        
        _state.update { 
            it.copy(
                elements = it.elements + newElement,
                nextLevel = getWeightedRandomLevel()
            )
        }
    }

    private fun updatePhysics() {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.isVictory) return

        val elements = currentState.elements.toMutableList()
        
        // 1. Apply Gravity and Movement
        for (i in elements.indices) {
            val el = elements[i]
            if (el.isStatic) continue

            val newVy = el.vy + GRAVITY
            val newX = el.x + el.vx
            val newY = el.y + newVy
            
            elements[i] = el.copy(
                x = newX,
                y = newY,
                vx = el.vx * FRICTION,
                vy = newVy
            )
        }

        // 2. Wall and Floor Collisions
        for (i in elements.indices) {
            val el = elements[i]
            val radius = BASE_RADIUS * el.level.radiusScale
            
            var nx = el.x
            var ny = el.y
            var nvx = el.vx
            var nvy = el.vy
            var nIsStatic = el.isStatic

            // Horizontal walls
            if (nx - radius < 0) {
                nx = radius
                nvx = -nvx * BOUNCE
            } else if (nx + radius > 1000f) {
                nx = 1000f - radius
                nvx = -nvx * BOUNCE
            }

            // Floor
            if (ny + radius > 1000f) {
                ny = 1000f - radius
                nvy = -nvy * BOUNCE
                nvx *= 0.8f // High friction on floor
                
                // Stabilization / Sleep
                if (nvy.absoluteValue < SLEEP_THRESHOLD && nvx.absoluteValue < SLEEP_THRESHOLD) {
                    nvy = 0f
                    nvx = 0f
                    nIsStatic = true
                }
            }

            elements[i] = el.copy(x = nx, y = ny, vx = nvx, vy = nvy, isStatic = nIsStatic)
        }

        // 3. Circle Collisions and Merging
        var scoreAdd = 0
        var victoryTriggered = false
        val toRemove = mutableSetOf<String>()
        val toAdd = mutableListOf<CodeElement>()

        for (i in elements.indices) {
            for (j in i + 1 until elements.size) {
                val e1 = elements[i]
                val e2 = elements[j]
                if (toRemove.contains(e1.id) || toRemove.contains(e2.id)) continue

                val r1 = BASE_RADIUS * e1.level.radiusScale
                val r2 = BASE_RADIUS * e2.level.radiusScale
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                val distSq = dx * dx + dy * dy
                val minDist = r1 + r2

                if (distSq < minDist * minDist) {
                    val dist = sqrt(distSq)
                    if (e1.level == e2.level) {
                        if (e1.level == CodeLevel.PROJECT_COMPLETE) {
                            victoryTriggered = true
                        } else {
                            // MERGE!
                            val nextLvl = e1.level.next() ?: continue
                            toRemove.add(e1.id)
                            toRemove.add(e2.id)
                            toAdd.add(CodeElement(
                                x = (e1.x + e2.x) / 2f,
                                y = (e1.y + e2.y) / 2f,
                                level = nextLvl,
                                vx = 0f,
                                vy = 0.5f // Slight push down for the new item
                            ))
                            scoreAdd += nextLvl.score
                        }
                    } else {
                        // Resolve collision (Static Resolution)
                        val overlap = (minDist - dist) + 0.1f // Add tiny buffer to stop vibration
                        val nx = dx / dist
                        val ny = dy / dist
                        
                        // Push elements apart
                        val ratio1 = if (e2.isStatic) 1f else 0.5f
                        val ratio2 = if (e1.isStatic) 1f else 0.5f
                        
                        elements[i] = elements[i].copy(
                            x = elements[i].x - nx * overlap * ratio1,
                            y = elements[i].y - ny * overlap * ratio1,
                            vx = elements[i].vx * 0.9f,
                            vy = elements[i].vy * 0.9f
                        )
                        elements[j] = elements[j].copy(
                            x = elements[j].x + nx * overlap * ratio2,
                            y = elements[j].y + ny * overlap * ratio2,
                            vx = elements[j].vx * 0.9f,
                            vy = elements[j].vy * 0.9f
                        )
                        
                        // If they are moving very slowly after collision, set to static
                        if (elements[i].vy.absoluteValue < SLEEP_THRESHOLD && elements[i].vx.absoluteValue < SLEEP_THRESHOLD && elements[i].y > 500f) {
                            elements[i] = elements[i].copy(isStatic = true, vx = 0f, vy = 0f)
                        }
                        if (elements[j].vy.absoluteValue < SLEEP_THRESHOLD && elements[j].vx.absoluteValue < SLEEP_THRESHOLD && elements[j].y > 500f) {
                            elements[j] = elements[j].copy(isStatic = true, vx = 0f, vy = 0f)
                        }
                    }
                }
            }
        }

        val finalElements = elements.filterNot { toRemove.contains(it.id) } + toAdd

        // 4. Check Loss Line
        var isAboveLine = false
        for (el in finalElements) {
            val radius = BASE_RADIUS * el.level.radiusScale
            if (el.y - radius < LOSS_LINE_Y && el.vy.absoluteValue < SLEEP_THRESHOLD && el.y > 100f) {
                isAboveLine = true
                break
            }
        }

        var gameOver = false
        if (isAboveLine) {
            if (gameOverTimer == 0L) {
                gameOverTimer = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - gameOverTimer > 1500) {
                gameOver = true
            }
        } else {
            gameOverTimer = 0L
        }

        _state.update { 
            it.copy(
                elements = finalElements,
                currentScore = it.currentScore + scoreAdd,
                isGameOver = gameOver,
                isVictory = victoryTriggered || it.isVictory
            )
        }
    }

    private fun submitScore(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            service.saveResult(MergeRunResult(playerName = name, score = _state.value.currentScore))
            _state.update { it.copy(isLoading = false) }
        }
    }

    private val Float.absoluteValue: Float get() = if (this < 0) -this else this
}
