package com.android.mobile.games.app.games.razarun.data

interface RazaGameService {
    suspend fun getTopRankings(): List<RankingEntry>
    suspend fun submitResult(name: String, distance: Float, status: String): Boolean
}

data class RankingEntry(
    val name: String,
    val distance: Float,
    val status: String
)
