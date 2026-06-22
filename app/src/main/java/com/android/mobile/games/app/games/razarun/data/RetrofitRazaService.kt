package com.android.mobile.games.app.games.razarun.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RazaRetrofitClient {
    // Puerto 8001 para La Raza Run (misma IP WSL que Code Slasher)
    private const val BASE_URL = "http://172.22.80.1:8001/"

    val instance: RazaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RazaApiService::class.java)
    }
}

class RetrofitRazaService : RazaGameService {

    override suspend fun getTopRankings(): List<RankingEntry> {
        return try {
            RazaRetrofitClient.instance.getRankings().map {
                RankingEntry(
                    name = it.name,
                    distance = it.distance,
                    status = it.status
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun submitResult(name: String, distance: Float, status: String): Boolean {
        return try {
            RazaRetrofitClient.instance.submitResult(
                RazaResultRequest(name = name, distance = distance, status = status)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
