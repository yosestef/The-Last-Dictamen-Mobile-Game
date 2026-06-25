package com.android.mobile.games.app.games.razarun.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.android.mobile.games.app.R
import com.android.mobile.games.app.games.razarun.model.RazaGameState
import com.android.mobile.games.app.games.razarun.model.RazaObstacleType
import com.android.mobile.games.app.games.razarun.model.RazaPlayerAction

@Composable
fun RazaCanvas(
    state: RazaGameState
) {
    val context = LocalContext.current

    // Pre-carga de todos los frames en el primer compose — nunca se redecodifica en recomposiciones
    val playerRunFrames = remember {
        listOf(
            R.drawable.correr_raza1,  R.drawable.correr_raza2,  R.drawable.correr_raza3,
            R.drawable.correr_raza4,  R.drawable.correr_raza5,  R.drawable.correr_raza6,
            R.drawable.correr_raza7,  R.drawable.correr_raza8,  R.drawable.correr_raza9,
            R.drawable.correr_raza10, R.drawable.correr_raza11, R.drawable.correr_raza12,
            R.drawable.correr_raza13, R.drawable.correr_raza14, R.drawable.correr_raza15,
            R.drawable.correr_raza16, R.drawable.correr_raza17, R.drawable.correr_raza18,
            R.drawable.correr_raza19, R.drawable.correr_raza20
        ).map { id -> BitmapFactory.decodeResource(context.resources, id).asImageBitmap() }
    }

    val playerJumpFrames = remember {
        listOf(
            R.drawable.raza_saltar1, R.drawable.raza_saltar2,
            R.drawable.raza_saltar3, R.drawable.raza_saltar4
        ).map { id -> BitmapFactory.decodeResource(context.resources, id).asImageBitmap() }
    }

    val playerSlideFrames = remember {
        listOf(
            R.drawable.raza_deslizar1, R.drawable.raza_deslizar2, R.drawable.raza_deslizar3
        ).map { id -> BitmapFactory.decodeResource(context.resources, id).asImageBitmap() }
    }

    val inicioImg = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_inicio).asImageBitmap() }
    val fondoImg  = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_fondo).asImageBitmap() }
    val finImg    = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_fin).asImageBitmap() }

    val carritoImg = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_carrito).asImageBitmap() }
    val mochilaImg = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_mochilas).asImageBitmap() }
    val charcoImg  = remember { BitmapFactory.decodeResource(context.resources, R.drawable.raza_charco).asImageBitmap() }

    // Selección de frame sin allocations — lookup simple en lista ya cargada
    val playerImg = when (state.playerAction) {
        RazaPlayerAction.RUN   -> playerRunFrames[state.animationFrame % 20]
        RazaPlayerAction.JUMP  -> playerJumpFrames[state.animationFrame % 4]
        RazaPlayerAction.SLIDE -> playerSlideFrames[state.animationFrame % 3]
        RazaPlayerAction.CRASH -> playerRunFrames[0]
        RazaPlayerAction.WIN   -> finImg
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background phases
        when {
            state.distance < 20f -> {
                val offset = (state.distance / 20f) * width
                drawImage(
                    image = inicioImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(-offset.toInt(), 0)
                )
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(width.toInt() - offset.toInt(), 0)
                )
            }
            state.distance < 580f -> {
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
                val finDistance = state.distance - 580f
                val progress = finDistance / 20f
                val offset = progress * width
                drawImage(
                    image = fondoImg,
                    dstSize = IntSize(width.toInt(), height.toInt()),
                    dstOffset = IntOffset(-offset.toInt(), 0)
                )
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
                RazaObstacleType.CHARCO  -> charcoImg
            }
            drawImage(
                image = obsImg,
                dstSize = IntSize(obs.width.toInt(), obs.height.toInt()),
                dstOffset = IntOffset(obs.x.toInt(), (obs.y * scaleY).toInt())
            )
        }

        // Draw player
        val basePlayerY = when (state.playerAction) {
            RazaPlayerAction.JUMP  -> 250f
            RazaPlayerAction.SLIDE -> 390f
            else                   -> 350f
        }
        drawImage(
            image = playerImg,
            dstSize = IntSize(200, 200),
            dstOffset = IntOffset(150, (basePlayerY * scaleY).toInt())
        )
    }
}
