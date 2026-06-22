package com.android.mobile.games.app.games.catchgame.model

enum class TriviaCategory(val displayName: String) {
    MATH("Math"),
    GEOGRAPHY("Geography"),
    SCIENCE("Science"),
    HISTORY("History");

    companion object {
        fun fromValue(value: String): TriviaCategory {
            return entries.firstOrNull { category ->
                category.name.equals(value, ignoreCase = true)
            } ?: SCIENCE
        }
    }
}
