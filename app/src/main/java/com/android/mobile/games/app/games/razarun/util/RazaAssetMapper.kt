package com.android.mobile.games.app.games.razarun.util

import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.razarun.model.RazaObstacleType
import com.android.mobile.games.app.games.razarun.model.RazaPlayerAction

object RazaAssetMapper {
    private val runFrames = listOf(
        R.drawable.correr_raza1,  R.drawable.correr_raza2,  R.drawable.correr_raza3,
        R.drawable.correr_raza4,  R.drawable.correr_raza5,  R.drawable.correr_raza6,
        R.drawable.correr_raza7,  R.drawable.correr_raza8,  R.drawable.correr_raza9,
        R.drawable.correr_raza10, R.drawable.correr_raza11, R.drawable.correr_raza12,
        R.drawable.correr_raza13, R.drawable.correr_raza14, R.drawable.correr_raza15,
        R.drawable.correr_raza16, R.drawable.correr_raza17, R.drawable.correr_raza18,
        R.drawable.correr_raza19, R.drawable.correr_raza20
    )

    fun getPlayerImage(action: RazaPlayerAction, frame: Int): Int {
        return when (action) {
            RazaPlayerAction.RUN   -> runFrames[frame % 20]
            RazaPlayerAction.JUMP  -> when (frame % 4) {
                0    -> R.drawable.raza_saltar1
                1    -> R.drawable.raza_saltar2
                2    -> R.drawable.raza_saltar3
                else -> R.drawable.raza_saltar4
            }
            RazaPlayerAction.SLIDE -> when (frame % 3) {
                0    -> R.drawable.raza_deslizar1
                1    -> R.drawable.raza_deslizar2
                else -> R.drawable.raza_deslizar3
            }
            RazaPlayerAction.CRASH -> runFrames[0]
            RazaPlayerAction.WIN   -> R.drawable.raza_fin
        }
    }

    fun getObstacleImage(type: RazaObstacleType): Int {
        return when (type) {
            RazaObstacleType.CARRETO -> R.drawable.raza_carrito
            RazaObstacleType.MOCHILA -> R.drawable.raza_mochilas
            RazaObstacleType.CHARCO  -> R.drawable.raza_charco
        }
    }
}
