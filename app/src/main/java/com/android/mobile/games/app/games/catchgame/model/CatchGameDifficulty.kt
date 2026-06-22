package com.android.mobile.games.app.games.catchgame.model

enum class CatchGameDifficulty(
    val label: String,
    val description: String,
    val spawnIntervalMs: Long,
    val badObjectProbability: Float,
    val minFallSpeedHeightRatio: Float,
    val maxFallSpeedHeightRatio: Float
) {

    EASY(
        label = "1er Semestre",
        description = "Pocas tareas, más café.",
        spawnIntervalMs = 900L,
        badObjectProbability = 0.24f,
        minFallSpeedHeightRatio = 0.32f,
        maxFallSpeedHeightRatio = 0.46f
    ),

    MEDIUM(
        label = "Medio Semestre",
        description = "Más reportes y menos tiempo.",
        spawnIntervalMs = 700L,
        badObjectProbability = 0.42f,
        minFallSpeedHeightRatio = 0.39f,
        maxFallSpeedHeightRatio = 0.58f
    ),

    HARD(
        label = "Semestre Final",
        description = "Lluvia de ETS y departamentales.",
        spawnIntervalMs = 520L,
        badObjectProbability = 0.60f,
        minFallSpeedHeightRatio = 0.48f,
        maxFallSpeedHeightRatio = 0.74f
    )
}
