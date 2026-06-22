package com.android.mobile.games.app.games.razarun.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class RazaResultRequest(
    val name: String,
    val distance: Float,
    val status: String
)

data class RazaRankingResponse(
    val name: String,
    val distance: Float,
    val status: String,
    val created_at: String
)

interface RazaApiService {
    @POST("results/")
    suspend fun submitResult(@Body result: RazaResultRequest): RazaRankingResponse

    @GET("rankings/")
    suspend fun getRankings(@Query("limit") limit: Int = 10): List<RazaRankingResponse>
}
