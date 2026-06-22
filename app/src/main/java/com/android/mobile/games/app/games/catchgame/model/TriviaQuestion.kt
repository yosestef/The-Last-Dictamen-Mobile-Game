package com.android.mobile.games.app.games.catchgame.model

data class TriviaQuestion(
    val id: Int,
    val category: TriviaCategory,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)
