package com.android.mobile.games.app.games.fruitninja.model

import androidx.compose.ui.geometry.Offset

enum class FruitNinjaEffectType {
    SPLASH,
    HALF_ONE,
    HALF_TWO,
    EXPLOSION
}

data class FruitNinjaEffect(
    val id: Long,
    val itemType: FruitNinjaItemType,
    val effectType: FruitNinjaEffectType,
    val position: Offset,
    val velocity: Offset = Offset.Zero,
    val size: Float,
    val ageFrames: Int = 0,
    val maxAgeFrames: Int
)