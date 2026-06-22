package com.android.mobile.games.app.games.fruitninja.assets

import androidx.annotation.DrawableRes
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItemType

data class FruitNinjaItemAssets(
    @DrawableRes val whole: Int,
    @DrawableRes val halfOne: Int?,
    @DrawableRes val halfTwo: Int?,
    @DrawableRes val splash: Int?
)

@DrawableRes
fun fruitNinjaBackgroundAsset(): Int = R.drawable.fondo_lab_slash

@DrawableRes
fun fruitNinjaExplosionAsset(): Int = R.drawable.fruit_ninja_explosion

fun FruitNinjaItemType.assets(): FruitNinjaItemAssets = when (this) {
    FruitNinjaItemType.BUG -> FruitNinjaItemAssets(R.drawable.bug, R.drawable.bug_izq, R.drawable.bug_der, null)
    FruitNinjaItemType.ERROR -> FruitNinjaItemAssets(R.drawable.error, R.drawable.error_izq, R.drawable.error_der, null)
    FruitNinjaItemType.NULO -> FruitNinjaItemAssets(R.drawable.nulo, R.drawable.nulo_izq, R.drawable.nulo_der, null)
    FruitNinjaItemType.IPN_CARD -> FruitNinjaItemAssets(R.drawable.ipn_card, R.drawable.ipn_card_izq, R.drawable.ipn_card_der, null)
    FruitNinjaItemType.CAFE_TACHADO -> FruitNinjaItemAssets(R.drawable.cafe_tachado, R.drawable.cafe_tachado_izq, R.drawable.cafe_tachado_der, null)
}