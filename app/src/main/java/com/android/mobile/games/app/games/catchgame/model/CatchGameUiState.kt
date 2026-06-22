package com.android.mobile.games.app.games.catchgame.model

data class CatchGameUiState(
    val score: Int = 0,
    val lives: Int = 3,
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val items: List<CatchGameItem> = emptyList(),
    val isGameOver: Boolean = false,
    val rescueChanceUsed: Boolean = false,
    val activeTriviaQuestion: TriviaQuestion? = null,
    val isTriviaVisible: Boolean = false,
    val triviaTimeLeftSeconds: Int = 0,
    val triviaFeedbackMessage: String? = null,
    val isTriviaAnswerLocked: Boolean = false,
    val triviaAnswerWasCorrect: Boolean? = null,
    val missedCount: Int = 0
)
