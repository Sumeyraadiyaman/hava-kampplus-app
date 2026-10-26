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
        // 1) Hareketli hava efekti (yağmur, kar, güneş...)
        HavaEfektKatmani(efekt = gorunum.efekt)

        // 2) Derinlik: üstte hafif ışık, altta hafif gölge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                )
        )

        // 3) Telefon koyu temadaysa her şeyi biraz karart
        if (koyuTema) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
        }

        // 4) İçerik: tüm yazılar varsayılan olarak beyaz
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            icerik()
        }
    }
}
