package com.android.mobile.games.app.games.fruitninja.model

import androidx.compose.ui.geometry.Offset

data class FruitNinjaItem(
    val id: Long,
    val type: FruitNinjaItemType,

    val position: Offset,
    val velocity: Offset,

    val radius: Float,

    val isSliced: Boolean = false,
    val isExploding: Boolean = false
)