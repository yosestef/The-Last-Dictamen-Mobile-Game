package com.android.mobile.games.app.games.fruitninja.util

import androidx.compose.ui.geometry.Offset
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItem
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItemType
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

fun createRandomFruitNinjaItem(
    id: Long,
    screenWidth: Float,
    screenHeight: Float,
    difficulty: FruitNinjaDifficulty
): FruitNinjaItem {
    val type = createRandomItemType(difficulty)
    val radius = getItemRadius(type)

    // 1. MARGEN DE SEGURIDAD
    // Evitamos que el ítem aparezca en los bordes extremos (izquierdo o derecho)
    val margin = radius * 2
    val usableWidth = screenWidth - (margin * 2)
    val startX = (Random.nextFloat() * usableWidth) + margin

    // El ítem nace justo debajo del borde inferior de la pantalla
    val startY = screenHeight + radius

    // 2. VELOCIDAD HORIZONTAL INTELIGENTE
    // Si el ítem nace en la mitad izquierda, le damos velocidad hacia la DERECHA
    // Si nace en la mitad derecha, le damos velocidad hacia la IZQUIERDA
    val centerScreen = screenWidth / 2
    val forceTowardsCenter = if (startX < centerScreen) 3f else -3f

    // Le sumamos un poco de aleatoriedad para que las trayectorias varíen
    val velocityX = forceTowardsCenter + (Random.nextFloat() * 2f - 1f)

    // 3. VELOCIDAD VERTICAL (Hacia arriba)
    val baseVelocityY = -(Random.nextFloat() * 14f + 16f)
    val velocityY = baseVelocityY * difficulty.initialSpeedMultiplier

    return FruitNinjaItem(
        id = id,
        type = type,
        position = Offset(startX, startY),
        velocity = Offset(velocityX, velocityY),
        radius = radius
    )
}

fun isPointInsideItem(
    point: Offset,
    item: FruitNinjaItem
): Boolean {
    val distance = sqrt(
        (point.x - item.position.x).pow(2) +
                (point.y - item.position.y).pow(2)
    )

    return distance <= item.radius
}


private fun createRandomItemType(difficulty: FruitNinjaDifficulty): FruitNinjaItemType {
    val roll = Random.nextInt(100)
    return when {
        // En Modo Relax NO hay café tachado
        roll < difficulty.penaltyProbability && difficulty != FruitNinjaDifficulty.RELAX -> FruitNinjaItemType.CAFE_TACHADO
        // Bonus de IPN Card solo en Salvar el Semestre
        roll > 92 && difficulty == FruitNinjaDifficulty.SAVE_SEMESTER -> FruitNinjaItemType.IPN_CARD
        else -> listOf(FruitNinjaItemType.BUG, FruitNinjaItemType.ERROR, FruitNinjaItemType.NULO).random()
    }
}

private fun getItemRadius(type: FruitNinjaItemType): Float = when (type) {
    FruitNinjaItemType.IPN_CARD -> 85f
    FruitNinjaItemType.CAFE_TACHADO -> 75f
    else -> 80f
}