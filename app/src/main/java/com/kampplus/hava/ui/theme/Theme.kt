package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AcikRenkler = lightColorScheme(
    primary = Color(0xFF2F6FD6),
    onPrimary = Color.White,
    secondary = Color(0xFFFF8A50),
    onSecondary = Color.White,
    background = Color(0xFFF4F7FC),
    surface = Color(0xFFF4F7FC)
)

private val KoyuRenkler = darkColorScheme(
    primary = Color(0xFF9CC2FF),
    onPrimary = Color(0xFF0B1A33),
    secondary = Color(0xFFFFB085),
    onSecondary = Color(0xFF3A1A00),
    background = Color(0xFF0E1525),
    surface = Color(0xFF0E1525)
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun HavaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KoyuRenkler else AcikRenkler,
        typography = Typography,
        content = content
    )
}
