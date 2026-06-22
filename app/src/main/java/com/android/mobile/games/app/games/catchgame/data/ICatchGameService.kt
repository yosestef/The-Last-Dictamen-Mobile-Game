package com.android.mobile.games.app.games.catchgame.data

data class CatchGameRankingEntry(
    val username: String,
    val score: Int,
    val difficulty: String,
    val createdAt: String
)

interface ICatchGameService {
    suspend fun submitScore(username: String, score: Int, difficulty: String): Boolean
    suspend fun getRankings(difficulty: String? = null, limit: Int = 10): List<CatchGameRankingEntry>
}
