package com.kampplus.hava.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
    val renkler = gorunum.renkler

    // Hava değişince renkler birden değil, yumuşakça geçsin
    val renk1 by animateColorAsState(renkler.first(), tween(900), label = "renk1")
    val renk2 by animateColorAsState(renkler.getOrElse(1) { renkler.last() }, tween(900), label = "renk2")
    val renk3 by animateColorAsState(renkler.last(), tween(900), label = "renk3")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(renk1, renk2, renk3)))
    ) {
        HavaEfektKatmani(efekt = gorunum.efekt, modifier = Modifier.fillMaxSize())

        if (koyuTema) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            icerik()
        }
    }
}
