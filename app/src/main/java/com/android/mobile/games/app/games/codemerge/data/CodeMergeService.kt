package com.android.mobile.games.app.games.codemerge.data

import com.android.mobile.games.app.games.codemerge.model.MergeRunResult
import kotlinx.coroutines.delay

interface ICodeMergeGameService {
    suspend fun saveResult(result: MergeRunResult): Boolean
    suspend fun getHighScores(): List<MergeRunResult>
}

class MockCodeMergeGameService : ICodeMergeGameService {
    private val scores = mutableListOf<MergeRunResult>()

    override suspend fun saveResult(result: MergeRunResult): Boolean {
        delay(500L) // Simulate network latency
        scores.add(result)
        scores.sortByDescending { it.score }
        return true
    }

    override suspend fun getHighScores(): List<MergeRunResult> {
        delay(500L) // Simulate network latency
        return scores.take(10)
    }
}
