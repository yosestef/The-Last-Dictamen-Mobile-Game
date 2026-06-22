package com.android.mobile.games.app.games.fruitninja.model

import androidx.compose.ui.graphics.Color

enum class FruitNinjaItemType {
    BUG, ERROR, NULO, IPN_CARD, CAFE_TACHADO
}

fun FruitNinjaItemType.isPenalty(): Boolean = this == FruitNinjaItemType.CAFE_TACHADO
fun FruitNinjaItemType.isBonus(): Boolean = this == FruitNinjaItemType.IPN_CARD

// se mantiene estas para que el motor siga funcionando sin errores
fun FruitNinjaItemType.isBomb(): Boolean = isPenalty()
fun FruitNinjaItemType.isFruit(): Boolean = !isPenalty()

fun FruitNinjaItemType.color(): Color = when (this) {
    FruitNinjaItemType.BUG -> Color(0xFF8BC34A)
    FruitNinjaItemType.ERROR -> Color(0xFFF44336)
    FruitNinjaItemType.NULO -> Color(0xFF9E9E9E)
    FruitNinjaItemType.IPN_CARD -> Color(0xFF2196F3)
    FruitNinjaItemType.CAFE_TACHADO -> Color(0xFF795548)
}