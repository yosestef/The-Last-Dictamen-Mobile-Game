package com.android.mobile.games.app.games.fruitninja.model

enum class FruitNinjaDifficulty(
    val label: String,
    val spawnIntervalFrames: Int,
    val initialSpeedMultiplier: Float,
    val gravity: Float,
    val penaltyProbability: Int,
    val maxItemsOnScreen: Int,
    val pointsPerItem: Int,
    val hasTimer: Boolean,
    val hasLives: Boolean
) {
    CLASSIC("Modo Clásico", 32, 1.0f, 0.50f, 15, 6, 10, false, true),
    SAVE_SEMESTER("Salvar el Semestre", 25, 1.3f, 0.60f, 20, 8, 15, true, true),
    RELAX("Modo Relax", 38, 0.8f, 0.40f, 0, 5, 5, true, false)
}