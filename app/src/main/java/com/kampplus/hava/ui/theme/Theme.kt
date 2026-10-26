package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AcikRenkler = lightColorScheme(
    primary = GokMavisi,
    onPrimary = Color.White,
    secondary = GunesTuruncusu,
    onSecondary = Color.White,
    background = AcikZemin,
    surface = AcikZemin,
    onBackground = KoyuYazi,
    onSurface = KoyuYazi
)

private val KoyuRenkler = darkColorScheme(
    primary = GokMavisiAcik,
    onPrimary = GeceZemin,
    secondary = GunesTuruncusuAcik,
    onSecondary = GeceZemin,
    background = GeceZemin,
    surface = GeceZemin
)

@Composable
fun HavaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KoyuRenkler else AcikRenkler,
        typography = Typography,
        content = content
    )
}
