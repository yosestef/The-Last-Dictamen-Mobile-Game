package com.android.mobile.games.app.games.razarun.util

import com.android.mobile.games.app.games.razarun.model.RazaObstacleType
import com.android.mobile.games.app.games.razarun.model.RazaPlayerAction

object RazaAssetMapper {
    fun getPlayerImage(action: RazaPlayerAction, frame: Int): Int {
        return when (action) {
            RazaPlayerAction.RUN -> {
                val f = (frame % 5) + 1
                when (f) {
                    1 -> com.android.mobile.games.app.R.drawable.raza_correr1
                    2 -> com.android.mobile.games.app.R.drawable.raza_correr2
                    3 -> com.android.mobile.games.app.R.drawable.raza_correr3
                    4 -> com.android.mobile.games.app.R.drawable.raza_correr4
                    else -> com.android.mobile.games.app.R.drawable.raza_correr5
                }
            }
            RazaPlayerAction.JUMP -> {
                val f = (frame % 4) + 1
                when (f) {
                    1 -> com.android.mobile.games.app.R.drawable.raza_saltar1
                    2 -> com.android.mobile.games.app.R.drawable.raza_saltar2
                    3 -> com.android.mobile.games.app.R.drawable.raza_saltar3
                    else -> com.android.mobile.games.app.R.drawable.raza_saltar4
                }
            }
            RazaPlayerAction.SLIDE -> {
                val f = (frame % 3) + 1
                when (f) {
                    1 -> com.android.mobile.games.app.R.drawable.raza_deslizar1
                    2 -> com.android.mobile.games.app.R.drawable.raza_deslizar2
                    else -> com.android.mobile.games.app.R.drawable.raza_deslizar3
                }
            }
            RazaPlayerAction.CRASH -> com.android.mobile.games.app.R.drawable.raza_correr1 // Static for now
            RazaPlayerAction.WIN -> com.android.mobile.games.app.R.drawable.raza_fin
        }
    }

    fun getObstacleImage(type: RazaObstacleType): Int {
        return when (type) {
            RazaObstacleType.CARRETO -> com.android.mobile.games.app.R.drawable.raza_carrito
            RazaObstacleType.MOCHILA -> com.android.mobile.games.app.R.drawable.raza_mochilas
            RazaObstacleType.CHARCO -> com.android.mobile.games.app.R.drawable.raza_charco
        }
    }
}
