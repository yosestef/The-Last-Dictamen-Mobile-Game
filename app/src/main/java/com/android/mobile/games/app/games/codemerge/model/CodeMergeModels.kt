package com.android.mobile.games.app.games.codemerge.model

import java.util.UUID

data class MergeRunResult(
    val id: String = UUID.randomUUID().toString(),
    val playerName: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CodeLevel(val score: Int, val radiusScale: Float) {
    NULO(10, 1.5f),           // Nivel 1: nulo.png (30dp)
    BUG(20, 2.25f),           // Nivel 2: bug.png (+25%)
    ERROR(30, 3.50f),         // Nivel 3: error.png (+50%)
    CAFE(50, 4.75f),          // Nivel 4: cafe_con_vida.png (+75%)
    IPN_CARD(100, 6.0f),      // Nivel 5: ipn_card.png (+100%)
    PROJECT_COMPLETE(500, 7.0f); // Nivel 6: proyecto_compilado.png (120dp / 30dp = 4x)

    fun next(): CodeLevel? = entries.getOrNull(ordinal + 1)
}

data class CodeElement(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val level: CodeLevel,
    val isStatic: Boolean = false
)
