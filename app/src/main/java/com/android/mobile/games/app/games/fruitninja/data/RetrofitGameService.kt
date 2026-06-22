package com.android.mobile.games.app.games.fruitninja.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://172.22.80.1:8000/"

    val instance: GameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GameApiService::class.java)
    }
}

class RetrofitGameService : GameService {

    override suspend fun getRanking(): List<Pair<String, Int>> {
        return try {
            val response = RetrofitClient.instance.getRankings()
            response.map { it.username to it.score }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun uploadScore(username: String, score: Int, difficulty: String) {
        try {
            RetrofitClient.instance.uploadScore(
                ScoreRequest(username, score, difficulty)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
