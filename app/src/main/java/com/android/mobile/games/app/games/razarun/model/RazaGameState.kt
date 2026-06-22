package com.android.mobile.games.app.games.razarun.model

enum class RazaPlayerAction {
    RUN, JUMP, SLIDE, CRASH, WIN
}

data class RazaGameState(
    val distance: Float = 0.0f,
    val timeRemaining: Long = 90000L, // 90 seconds in ms
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val playerAction: RazaPlayerAction = RazaPlayerAction.RUN,
    val obstacles: List<RazaObstacle> = emptyList(),
    val currentSpeed: Float = 5.0f,
    val animationFrame: Int = 0
)

data class RazaObstacle(
    val x: Float,
    val y: Float,
    val type: RazaObstacleType,
    val width: Float = 100f,
    val height: Float = 100f
)

enum class RazaObstacleType {
    CARRETO, MOCHILA, CHARCO
}
