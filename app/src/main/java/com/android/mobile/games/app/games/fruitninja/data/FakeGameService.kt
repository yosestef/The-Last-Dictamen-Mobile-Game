package com.android.mobile.games.app.games.fruitninja.data

class FakeGameService : GameService {
    override suspend fun getRanking() = listOf(
        "Senior_Dev" to 15000,
        "Junior_IPN" to 8500,
        "Bug_Hunter" to 4200
    )

    override suspend fun uploadScore(username: String, score: Int, difficulty: String) {
        // Simulación de envío a FastAPI
        println("Enviando dictamen de $score puntos de $username ($difficulty) al servidor...")
    }
}
