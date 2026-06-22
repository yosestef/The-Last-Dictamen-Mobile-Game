package com.android.mobile.games.app.games.fruitninja.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ScoreRequest(
    val username: String,
    val score: Int,
    val difficulty: String
)

data class ScoreResponse(
    val id: Int? = null,
    val username: String,
    val score: Int,
    val difficulty: String,
    val created_at: String
)

interface GameApiService {
    @GET("rankings/")
    suspend fun getRankings(
        @Query("limit") limit: Int = 10
    ): List<ScoreResponse>

    @POST("scores/")
    suspend fun uploadScore(
        @Body score: ScoreRequest
    ): ScoreResponse
}
