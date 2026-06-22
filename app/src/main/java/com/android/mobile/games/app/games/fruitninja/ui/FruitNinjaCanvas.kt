package com.android.mobile.games.app.games.fruitninja.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.android.mobile.games.app.games.fruitninja.assets.assets
import com.android.mobile.games.app.games.fruitninja.assets.fruitNinjaBackgroundAsset
import com.android.mobile.games.app.games.fruitninja.assets.fruitNinjaExplosionAsset
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaEffect
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaEffectType
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaGameState
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItem
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaItemType
import kotlin.math.roundToInt

@Composable
fun FruitNinjaCanvas(
    gameState: FruitNinjaGameState,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    onSlice: (Offset) -> Unit
) {
    val slashTrail = remember {
        mutableStateListOf<Offset>()
    }

    val backgroundImage = ImageBitmap.imageResource(
        id = fruitNinjaBackgroundAsset()
    )

    val explosionImage = ImageBitmap.imageResource(
        id = fruitNinjaExplosionAsset()
    )

    val itemImages = rememberFruitNinjaItemImages()
    val effectImages = rememberFruitNinjaEffectImages()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        slashTrail.clear()
                        slashTrail.add(offset)
                        onSlice(offset)
                    },
                    onDrag = { change, _ ->
                        slashTrail.add(change.position)

                        if (slashTrail.size > 15) {
                            slashTrail.removeAt(0)
                        }

                        onSlice(change.position)
                    },
                    onDragEnd = {
                        slashTrail.clear()
                    },
                    onDragCancel = {
                        slashTrail.clear()
                    }
                )
            }
    ) {
        onCanvasSizeChanged(
            size.width,
            size.height
        )

        drawBackgroundImage(
            image = backgroundImage
        )

        gameState.effects.forEach { effect ->
            val image = when (effect.effectType) {
                FruitNinjaEffectType.EXPLOSION -> explosionImage
                else -> effectImages[effect.itemType]?.get(effect.effectType)
            }

            if (image != null) {
                drawFruitNinjaEffect(
                    effect = effect,
                    image = image
                )
            }
        }

        gameState.items.forEach { item ->
            val image = itemImages[item.type]

            if (image != null) {
                drawFruitNinjaItem(
                    item = item,
                    image = image
                )
            }
        }

        drawSlashTrail(
            points = slashTrail
        )
    }
}

@Composable
private fun rememberFruitNinjaItemImages(): Map<FruitNinjaItemType, ImageBitmap> {
    return FruitNinjaItemType.entries.associateWith { type ->
        ImageBitmap.imageResource(
            id = type.assets().whole
        )
    }
}

@Composable
private fun rememberFruitNinjaEffectImages(): Map<FruitNinjaItemType, Map<FruitNinjaEffectType, ImageBitmap>> {
    return FruitNinjaItemType.entries.associateWith { type ->
        val assets = type.assets()

        buildMap {
            assets.splash?.let { resourceId ->
                put(
                    FruitNinjaEffectType.SPLASH,
                    ImageBitmap.imageResource(id = resourceId)
                )
            }

            assets.halfOne?.let { resourceId ->
                put(
                    FruitNinjaEffectType.HALF_ONE,
                    ImageBitmap.imageResource(id = resourceId)
                )
            }

            assets.halfTwo?.let { resourceId ->
                put(
                    FruitNinjaEffectType.HALF_TWO,
                    ImageBitmap.imageResource(id = resourceId)
                )
            }
        }
    }
}

private fun DrawScope.drawBackgroundImage(
    image: ImageBitmap
) {
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(
            width = image.width,
            height = image.height
        ),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(
            width = size.width.roundToInt(),
            height = size.height.roundToInt()
        )
    )
}

private fun DrawScope.drawFruitNinjaItem(
    item: FruitNinjaItem,
    image: ImageBitmap
) {
    val imageSize = (item.radius * 3.0f).roundToInt()

    drawCenteredImage(
        image = image,
        center = item.position,
        imageSize = imageSize
    )
}

private fun DrawScope.drawFruitNinjaEffect(
    effect: FruitNinjaEffect,
    image: ImageBitmap
) {
    val imageSize = effect.size.roundToInt()

    drawCenteredImage(
        image = image,
        center = effect.position,
        imageSize = imageSize
    )
}

private fun DrawScope.drawCenteredImage(
    image: ImageBitmap,
    center: Offset,
    imageSize: Int
) {
    val topLeftX = (center.x - imageSize / 2f).roundToInt()
    val topLeftY = (center.y - imageSize / 2f).roundToInt()

    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(
            width = image.width,
            height = image.height
        ),
        dstOffset = IntOffset(
            x = topLeftX,
            y = topLeftY
        ),
        dstSize = IntSize(
            width = imageSize,
            height = imageSize
        )
    )
}

private fun DrawScope.drawSlashTrail(
    points: List<Offset>
) {
    if (points.size <= 1) return

    // Capa de brillo Lila Neón:)))
    for (index in 0 until points.lastIndex) {
        drawLine(
            color = Color(0xFFD100FF).copy(alpha = 0.5f), // Lila Neón transparente
            start = points[index],
            end = points[index + 1],
            strokeWidth = 22f, // Más gruesa para el brillooo
            cap = StrokeCap.Round
        )
    }

    // Capa central (Blanco/Lila claro)
    for (index in 0 until points.lastIndex) {
        drawLine(
            color = Color.White,
            start = points[index],
            end = points[index + 1],
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }
}