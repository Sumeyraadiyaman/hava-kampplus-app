package com.kampplus.hava.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun HavaArkaPlani(
    gorunum: HavaGorunumu,
    modifier: Modifier = Modifier,
    icerik: @Composable () -> Unit
) {
    val koyuTema = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gorunum.renkler))
    ) {
        if (koyuTema) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            icerik()
        }
    }
}