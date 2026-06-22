package com.android.mobile.games.app.games.razarun.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.razarun.model.RazaGameState
import com.android.mobile.games.app.games.razarun.model.RazaObstacleType
import com.android.mobile.games.app.games.razarun.model.RazaPlayerAction
import com.android.mobile.games.app.games.razarun.util.RazaAssetMapper

@Composable
fun RazaCanvas(
    state: RazaGameState
) {
    // Load background images (imageResource is already @Composable and handles its own caching)
    val inicioImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_inicio)
    val fondoImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_fondo)
    val finImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_fin)
    
    // Load player images
    val run1 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_correr1)
    val run2 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_correr2)
    val run3 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_correr3)
    val run4 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_correr4)
    val run5 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_correr5)
    val playerRunFrames = remember(run1, run2, run3, run4, run5) { listOf(run1, run2, run3, run4, run5) }

    val jump1 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_saltar1)
    val jump2 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_saltar2)
    val jump3 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_saltar3)
    val jump4 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_saltar4)
    val playerJumpFrames = remember(jump1, jump2, jump3, jump4) { listOf(jump1, jump2, jump3, jump4) }

    val slide1 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_deslizar1)
    val slide2 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_deslizar2)
    val slide3 = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_deslizar3)
    val playerSlideFrames = remember(slide1, slide2, slide3) { listOf(slide1, slide2, slide3) }

    // Load obstacles
    val carritoImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_carrito)
    val mochilaImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_mochilas)
    val charcoImg = ImageBitmap.imageResource(id = com.android.mobile.games.app.R.drawable.raza_charco)

    val playerImg = remember(state.playerAction, state.animationFrame, playerRunFrames, playerJumpFrames, playerSlideFrames, finImg) {
        when (state.playerAction) {
            RazaPlayerAction.RUN -> playerRunFrames[(state.animationFrame % 5)]
            RazaPlayerAction.JUMP -> playerJumpFrames[(state.animationFrame % 4)]
            RazaPlayerAction.SLIDE -> playerSlideFrames[(state.animationFrame % 3)]
            RazaPlayerAction.CRASH -> playerRunFrames[0]
            RazaPlayerAction.WIN -> finImg
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Background Phases
        when {
            state.distance < 20f -> {
                // Phase 1: Inicio (0m to 20m)
                // inicioImg is 20 meters long relative to view?
                // Let's assume the screen shows 20 meters at once for phase 1.
                val offset = (state.distance / 20f) * width
                
                // Draw inicio sliding out
                drawImage(
                    image = inicioImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(-offset.toInt(), 0)
                )
                
                // Draw fondo sliding in immediately behind it
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(width.toInt() - offset.toInt(), 0)
                )
            }
            state.distance < 580f -> {
                // Phase 2: Fondo ciclado (20m to 580m)
                // We use a speed of 10 meters per screen width for visual movement
                val tunnelDistance = state.distance - 20f
                val metersPerScreenWidth = 15f 
                val offset = ((tunnelDistance % metersPerScreenWidth) / metersPerScreenWidth) * width
                
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(-offset.toInt(), 0)
                )
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(width.toInt() - offset.toInt(), 0)
                )
            }
            else -> {
                // Phase 3: Fin (580m to 600m)
                val finDistance = state.distance - 580f
                val progress = finDistance / 20f
                val offset = progress * width
                
                // Draw last part of fondo sliding out
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(-offset.toInt(), 0)
                )
                
                // Draw fin sliding in
                drawImage(
                    image = finImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(width.toInt() - offset.toInt(), 0)
                )
            }
        }
        
        // Draw obstacles
        val scaleY = height / 500f
        state.obstacles.forEach { obs ->
            val obsImg = when (obs.type) {
                RazaObstacleType.CARRETO -> carritoImg
                RazaObstacleType.MOCHILA -> mochilaImg
                RazaObstacleType.CHARCO -> charcoImg
            }
            // Use the Y from the engine, scaled to screen height
            val obsY = obs.y * scaleY
            drawImage(
                image = obsImg,
                dstSize = IntSize(obs.width.toInt(), obs.height.toInt()),
                dstOffset = IntOffset(obs.x.toInt(), obsY.toInt())
            )
        }
        
        // Draw player
        val basePlayerY = when (state.playerAction) {
            RazaPlayerAction.JUMP -> 250f
            RazaPlayerAction.SLIDE -> 390f
            else -> 350f
        }
        val playerCanvasY = basePlayerY * scaleY
        
        drawImage(
            image = playerImg,
            dstSize = IntSize(200, 200),
            dstOffset = IntOffset(150, playerCanvasY.toInt()) // Match engine playerX=150
        )
    }
}

private fun getPlayerRunRes(frame: Int): Int = when (frame) {
    1 -> com.android.mobile.games.app.R.drawable.raza_correr1
    2 -> com.android.mobile.games.app.R.drawable.raza_correr2
    3 -> com.android.mobile.games.app.R.drawable.raza_correr3
    4 -> com.android.mobile.games.app.R.drawable.raza_correr4
    else -> com.android.mobile.games.app.R.drawable.raza_correr5
}

private fun getPlayerJumpRes(frame: Int): Int = when (frame) {
    1 -> com.android.mobile.games.app.R.drawable.raza_saltar1
    2 -> com.android.mobile.games.app.R.drawable.raza_saltar2
    3 -> com.android.mobile.games.app.R.drawable.raza_saltar3
    else -> com.android.mobile.games.app.R.drawable.raza_saltar4
}

private fun getPlayerSlideRes(frame: Int): Int = when (frame) {
    1 -> com.android.mobile.games.app.R.drawable.raza_deslizar1
    2 -> com.android.mobile.games.app.R.drawable.raza_deslizar2
    else -> com.android.mobile.games.app.R.drawable.raza_deslizar3
}
