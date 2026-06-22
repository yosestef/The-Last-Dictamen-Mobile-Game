package com.android.mobile.games.app.games.fruitninja.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val FRUIT_NINJA_SCORE_DATASTORE_NAME = "fruit_ninja_scores"

private val Context.fruitNinjaScoreDataStore by preferencesDataStore(
    name = FRUIT_NINJA_SCORE_DATASTORE_NAME
)

class FruitNinjaScoreRepository(
    private val context: Context
) {

    fun getBestScore(
        difficulty: FruitNinjaDifficulty
    ): Flow<Int> {
        val key = getBestScoreKey(difficulty)

        return context.fruitNinjaScoreDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun saveBestScoreIfNeeded(
        difficulty: FruitNinjaDifficulty,
        score: Int
    ) {
        val key = getBestScoreKey(difficulty)

        context.fruitNinjaScoreDataStore.edit { preferences ->
            val currentBestScore = preferences[key] ?: 0

            if (score > currentBestScore) {
                preferences[key] = score
            }
        }
    }

    private fun getBestScoreKey(
        difficulty: FruitNinjaDifficulty
    ) = intPreferencesKey(
        name = "best_score_${difficulty.name.lowercase()}"
    )
}