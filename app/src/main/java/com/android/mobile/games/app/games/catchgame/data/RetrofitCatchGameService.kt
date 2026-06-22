package com.android.mobile.games.app.games.catchgame.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CatchGameRetrofitClient {
    // Misma IP WSL que Code Slasher. Puerto 8003 para The Last Dictamen.
    private const val BASE_URL = "http://172.22.80.1:8003/"

    val instance: CatchGameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatchGameApiService::class.java)
    }
}

class RetrofitCatchGameService : ICatchGameService {

    override suspend fun submitScore(username: String, score: Int, difficulty: String): Boolean {
        return try {
            CatchGameRetrofitClient.instance.submitScore(
                CatchScoreRequest(
                    username = username,
                    score = score,
                    difficulty = difficulty
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getRankings(difficulty: String?, limit: Int): List<CatchGameRankingEntry> {
        return try {
            CatchGameRetrofitClient.instance.getRankings(
                difficulty = difficulty,
                limit = limit
            ).map { response ->
                CatchGameRankingEntry(
                    username = response.username,
                    score = response.score,
                    difficulty = response.difficulty,
                    createdAt = response.created_at
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
