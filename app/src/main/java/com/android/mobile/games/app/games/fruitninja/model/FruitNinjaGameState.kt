package com.android.mobile.games.app.games.fruitninja.model

data class FruitNinjaGameState(

    val items: List<FruitNinjaItem> = emptyList(),

    val effects: List<FruitNinjaEffect> = emptyList(),

    val score: Int = 0,

    val lives: Int = 3,

    val timeRemainingSeconds: Int = 60,

    val difficulty: FruitNinjaDifficulty =
        FruitNinjaDifficulty.CLASSIC,

    val isRunning: Boolean = false,

    val isPaused: Boolean = false,

    val isGameOver: Boolean = false,

    val bugsEliminated: Int = 0,

    val maxCombo: Int = 0
)