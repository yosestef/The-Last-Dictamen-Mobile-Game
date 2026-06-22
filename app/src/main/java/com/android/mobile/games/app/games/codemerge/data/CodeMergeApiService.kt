package com.android.mobile.games.app.games.codemerge.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CodeMergeScoreRequest(
    val player_name: String,
    val score: Int
)

data class CodeMergeScoreResponse(
    val id: Int,
    val player_name: String,
    val score: Int,
    val created_at: String
)

interface CodeMergeApiService {
    @POST("scores/")
    suspend fun uploadScore(@Body score: CodeMergeScoreRequest): CodeMergeScoreResponse

    @GET("highscores/")
    suspend fun getHighScores(@Query("limit") limit: Int = 10): List<CodeMergeScoreResponse>
}
