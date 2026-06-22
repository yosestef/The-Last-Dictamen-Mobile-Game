package com.android.mobile.games.app.games.codemerge.data

import com.android.mobile.games.app.games.codemerge.model.MergeRunResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CodeMergeRetrofitClient {
    // Puerto 8002 para Code Merge (misma IP WSL que Code Slasher)
    private const val BASE_URL = "http://172.22.80.1:8002/"

    val instance: CodeMergeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeMergeApiService::class.java)
    }
}

class RetrofitCodeMergeGameService : ICodeMergeGameService {

    override suspend fun saveResult(result: MergeRunResult): Boolean {
        return try {
            CodeMergeRetrofitClient.instance.uploadScore(
                CodeMergeScoreRequest(
                    player_name = result.playerName,
                    score = result.score
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getHighScores(): List<MergeRunResult> {
        return try {
            CodeMergeRetrofitClient.instance.getHighScores().map {
                MergeRunResult(
                    id = it.id.toString(),
                    playerName = it.player_name,
                    score = it.score,
                    timestamp = runCatching {
                        java.time.Instant.parse(it.created_at).toEpochMilli()
                    }.getOrDefault(System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
