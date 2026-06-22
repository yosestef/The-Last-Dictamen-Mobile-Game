package com.android.mobile.games.app.games.fruitninja.data

interface GameService {
    suspend fun getRanking(): List<Pair<String, Int>>
    suspend fun uploadScore(username: String, score: Int, difficulty: String)
}
