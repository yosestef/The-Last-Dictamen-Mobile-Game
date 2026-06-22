package com.android.mobile.games.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = CuteCream,
    surface = CuteCream
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = CuteCream,
    surface = CuteCream
)

@Composable
fun AndroidmobilegamesappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Forzamos dynamicColor a false para evitar problemas en Pixel 7
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Forzamos LightColorScheme para mantener la estética Kawaii independientemente del sistema
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}