package com.android.mobile.games.app.games.catchgame.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CatchScoreRequest(
    val username: String,
    val score: Int,
    val difficulty: String
)

data class CatchScoreResponse(
    val id: Int,
    val username: String,
    val score: Int,
    val difficulty: String,
    val created_at: String
)

data class CatchRankingResponse(
    val username: String,
    val score: Int,
    val difficulty: String,
    val created_at: String
)

interface CatchGameApiService {
    @POST("scores/")
    suspend fun submitScore(@Body score: CatchScoreRequest): CatchScoreResponse

    @GET("rankings/")
    suspend fun getRankings(
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10
    ): List<CatchRankingResponse>
}
