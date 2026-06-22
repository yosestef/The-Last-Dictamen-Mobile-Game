package com.android.mobile.games.app.games.razarun.data

import kotlinx.coroutines.delay

class FakeRazaService : RazaGameService {
    private val rankings = mutableListOf(
        RankingEntry("Juan", 600f, "VICTORY"),
        RankingEntry("Maria", 450f, "CRASHED"),
        RankingEntry("Pedro", 300f, "TIME_OUT")
    )

    override suspend fun getTopRankings(): List<RankingEntry> {
        delay(500) // Simulate network delay
        return rankings.sortedByDescending { it.distance }
    }

    override suspend fun submitResult(name: String, distance: Float, status: String): Boolean {
        delay(500) // Simulate network delay
        rankings.add(RankingEntry(name, distance, status))
        return true
    }
}
