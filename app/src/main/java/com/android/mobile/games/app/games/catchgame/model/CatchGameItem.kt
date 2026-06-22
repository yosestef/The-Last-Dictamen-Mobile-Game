package com.android.mobile.games.app.games.catchgame.model

import androidx.annotation.DrawableRes

data class CatchGameItem(
    val id: Long,
    val x: Float,
    val y: Float,
    val speedPxPerSecond: Float,
    @DrawableRes val drawableRes: Int,
    val isBad: Boolean
)
