package com.android.mobile.games.app.games.catchgame.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val CATCH_GAME_SCORE_DATASTORE_NAME = "catch_game_scores"

private val Context.catchGameScoreDataStore by preferencesDataStore(
    name = CATCH_GAME_SCORE_DATASTORE_NAME
)

class CatchGameScoreRepository(
    private val context: Context
) {

    fun getBestScore(
        difficulty: CatchGameDifficulty
    ): Flow<Int> {
        val key = bestScoreKey(difficulty)

        return context.catchGameScoreDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun saveBestScoreIfNeeded(
        difficulty: CatchGameDifficulty,
        score: Int
    ) {
        val key = bestScoreKey(difficulty)

        context.catchGameScoreDataStore.edit { preferences ->
            val currentBestScore = preferences[key] ?: 0

            if (score > currentBestScore) {
                preferences[key] = score
            }
        }
    }

    private fun bestScoreKey(
        difficulty: CatchGameDifficulty
    ) = intPreferencesKey(
        name = "best_score_${difficulty.name.lowercase()}"
    )
}
