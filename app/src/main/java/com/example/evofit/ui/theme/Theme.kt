package com.example.evofit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EvoColorScheme = darkColorScheme(
    primary = EvoGreen,
    onPrimary = EvoOnGreen,
    secondary = EvoGreen,
    background = EvoBackground,
    onBackground = Color.White,
    surface = EvoCard,
    onSurface = Color.White,
    outline = EvoBorder,
    error = EvoError
)

@Composable
fun EvoFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EvoColorScheme,
        typography = Typography,
        content = content
    )
}
