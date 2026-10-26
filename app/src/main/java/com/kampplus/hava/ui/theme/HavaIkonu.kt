package com.kampplus.hava.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.kampplus.hava.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val TAM_TUR = (2.0 * PI).toFloat()

/**
 * Emoji yerine kodla çizilen, gölgeli ve ışık yansımalı "3B" hava ikonu.
 * Her şey ikonun boyutuna göre orantılı çizildiği için 24 dp de olsa 160 dp de olsa net görünür.
 */
@Composable
fun HavaIkonu(
    kosul: WeatherCondition,
    modifier: Modifier = Modifier,
    gunduz: Boolean = true,
    animasyonlu: Boolean = true
) {
    val sonsuz = rememberInfiniteTransition(label = "ikon")
    val zaman by sonsuz.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ikonZamani"
    )

    Canvas(modifier = modifier) {
        val t = if (animasyonlu) zaman else 0f
        ikonCiz(kosul, gunduz, t)
    }
}

private fun DrawScope.ikonCiz(kosul: WeatherCondition, gunduz: Boolean, t: Float) {
    val s = min(size.width, size.height)
    val baslangic = Offset((size.width - s) / 2f, (size.height - s) / 2f)
    val suzulme = sin(t * TAM_TUR) * 0.015f

    // İkon içindeki (0..1, 0..1) konumu ekrandaki noktaya çevirir
    fun nokta(x: Float, y: Float): Offset =
        Offset(baslangic.x + x * s, baslangic.y + (y + suzulme) * s)

    when (kosul) {
        WeatherCondition.CLEAR -> {
            if (gunduz) {
                gunes(nokta(0.5f, 0.5f), s * 0.24f, t)
            } else {
                ay(nokta(0.5f, 0.5f), s * 0.27f)
                yildizlar(nokta(0.5f, 0.5f), s, t)
            }
        }

        WeatherCondition.MOSTLY_CLEAR, WeatherCondition.PARTLY_CLOUDY -> {
            if (gunduz) gunes(nokta(0.63f, 0.37f), s * 0.18f, t)
            else ay(nokta(0.64f, 0.36f), s * 0.19f)
            bulut(nokta(0.44f, 0.62f), s * 0.62f, koyuluk = 0f)
        }

        WeatherCondition.OVERCAST, WeatherCondition.UNKNOWN -> {
            bulut(nokta(0.63f, 0.42f), s * 0.46f, koyuluk = 0.45f)
            bulut(nokta(0.44f, 0.60f), s * 0.64f, koyuluk = 0.1f)
        }

        WeatherCondition.FOG -> {
            bulut(nokta(0.5f, 0.42f), s * 0.62f, koyuluk = 0.2f)
            sisCizgileri(nokta(0.5f, 0.74f), s, t)
        }

        WeatherCondition.DRIZZLE -> {
            bulut(nokta(0.5f, 0.42f), s * 0.66f, koyuluk = 0.15f)
            damlalar(nokta(0.5f, 0.70f), s * 0.66f, t, adet = 2)
        }

        WeatherCondition.RAIN -> {
            bulut(nokta(0.5f, 0.42f), s * 0.66f, koyuluk = 0.3f)
            damlalar(nokta(0.5f, 0.70f), s * 0.66f, t, adet = 3)
        }

        WeatherCondition.SHOWERS -> {
            if (gunduz) gunes(nokta(0.68f, 0.30f), s * 0.15f, t)
            else ay(nokta(0.68f, 0.30f), s * 0.16f)
            bulut(nokta(0.46f, 0.44f), s * 0.64f, koyuluk = 0.15f)
            damlalar(nokta(0.46f, 0.72f), s * 0.64f, t, adet = 3)
        }

        WeatherCondition.SNOW -> {
            bulut(nokta(0.5f, 0.42f), s * 0.66f, koyuluk = 0.05f)
            karTaneleri(nokta(0.5f, 0.75f), s * 0.66f, t)
        }

        WeatherCondition.THUNDERSTORM -> {
            bulut(nokta(0.5f, 0.40f), s * 0.68f, koyuluk = 0.9f)
            damlalar(nokta(0.5f, 0.70f), s * 0.80f, t, adet = 2)
            simsek(nokta(0.5f, 0.60f), s * 0.66f, t)
        }
    }
}

// ------------------------------------------------------------------ Parçalar

private fun DrawScope.gunes(merkez: Offset, r: Float, t: Float) {
    // Dış parıltı
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x88FFD54F), Color(0x00FFD54F)),
            center = merkez,
            radius = r * 2f
        ),
        radius = r * 2f,
        center = merkez
    )

    // Yavaşça dönen ışınlar
    rotate(degrees = t * 45f, pivot = merkez) {
        for (i in 0 until 8) {
            val aci = i * TAM_TUR / 8f
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFE082), Color(0xFFFFA000)),
                    start = Offset(merkez.x + cos(aci) * r * 1.25f, merkez.y + sin(aci) * r * 1.25f),
                    end = Offset(merkez.x + cos(aci) * r * 1.65f, merkez.y + sin(aci) * r * 1.65f)
                ),
                start = Offset(merkez.x + cos(aci) * r * 1.25f, merkez.y + sin(aci) * r * 1.25f),
                end = Offset(merkez.x + cos(aci) * r * 1.65f, merkez.y + sin(aci) * r * 1.65f),
                strokeWidth = r * 0.2f,
                cap = StrokeCap.Round
            )
        }
    }

    // Gövde: sol üstten aydınlatılmış küre
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFCA28), Color(0xFFFF8F00)),
            center = Offset(merkez.x - r * 0.35f, merkez.y - r * 0.35f),
            radius = r * 1.5f
        ),
        radius = r,
        center = merkez
    )

    // Parlama noktası
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = r * 0.25f,
        center = Offset(merkez.x - r * 0.38f, merkez.y - r * 0.4f)
    )
}

private fun DrawScope.ay(merkez: Offset, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x66B39DDB), Color(0x00B39DDB)),
            center = merkez,
            radius = r * 1.9f
        ),
        radius = r * 1.9f,
        center = merkez
    )

    val dolunay = Path().apply { addOval(Rect(center = merkez, radius = r)) }
    val kesik = Path().apply {
        addOval(Rect(center = Offset(merkez.x + r * 0.55f, merkez.y - r * 0.35f), radius = r * 0.85f))
    }
    val hilal = Path()
    hilal.op(dolunay, kesik, PathOperation.Difference)

    // Gölge
    translate(left = r * 0.06f, top = r * 0.1f) {
        drawPath(path = hilal, color = Color.Black.copy(alpha = 0.18f))
    }
    // Gövde
    drawPath(
        path = hilal,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF3EEFF), Color(0xFFC5B5F5), Color(0xFF7E68C8)),
            center = Offset(merkez.x - r * 0.45f, merkez.y - r * 0.2f),
            radius = r * 1.6f
        )
    )
    // Krater
    drawCircle(
        color = Color(0xFF6A55B5).copy(alpha = 0.25f),
        radius = r * 0.13f,
        center = Offset(merkez.x - r * 0.5f, merkez.y + r * 0.2f)
    )
    drawCircle(
        color = Color(0xFF6A55B5).copy(alpha = 0.2f),
        radius = r * 0.08f,
        center = Offset(merkez.x - r * 0.25f, merkez.y + r * 0.55f)
    )
}

private fun DrawScope.yildizlar(merkez: Offset, s: Float, t: Float) {
    val konumlar = listOf(
        Offset(0.30f, -0.28f), Offset(0.36f, 0.05f), Offset(-0.32f, -0.30f)
    )
    konumlar.forEachIndexed { i, k ->
        val parlaklik = 0.4f + 0.6f * ((sin((t + i * 0.3f) * TAM_TUR) + 1f) / 2f)
        val c = Offset(merkez.x + k.x * s, merkez.y + k.y * s)
        val boy = s * 0.045f * (0.7f + 0.3f * parlaklik)
        val renk = Color(0xFFFFE082).copy(alpha = parlaklik)
        drawLine(renk, Offset(c.x - boy, c.y), Offset(c.x + boy, c.y), strokeWidth = s * 0.012f, cap = StrokeCap.Round)
        drawLine(renk, Offset(c.x, c.y - boy), Offset(c.x, c.y + boy), strokeWidth = s * 0.012f, cap = StrokeCap.Round)
        drawCircle(color = renk, radius = s * 0.012f, center = c)
    }
}

/**
 * Kabarık bulut. koyuluk: 0 = bembeyaz, 1 = fırtına bulutu.
 */
private fun DrawScope.bulut(merkez: Offset, w: Float, koyuluk: Float) {
    val yol = Path().apply {
        addOval(Rect(center = Offset(merkez.x - w * 0.28f, merkez.y + w * 0.06f), radius = w * 0.22f))
        addOval(Rect(center = Offset(merkez.x + w * 0.02f, merkez.y - w * 0.08f), radius = w * 0.30f))
        addOval(Rect(center = Offset(merkez.x + w * 0.30f, merkez.y + w * 0.06f), radius = w * 0.22f))
        addRoundRect(
            RoundRect(
                left = merkez.x - w * 0.48f,
                top = merkez.y + w * 0.02f,
                right = merkez.x + w * 0.50f,
                bottom = merkez.y + w * 0.28f,
                cornerRadius = CornerRadius(w * 0.14f)
            )
        )
    }

    // Yumuşak gölge: bulutu havada asılı gösterir
    translate(top = w * 0.07f) {
        drawPath(path = yol, color = Color.Black.copy(alpha = 0.16f))
    }

    val ust = lerp(Color.White, Color(0xFFA3ABC2), koyuluk)
    val alt = lerp(Color(0xFFD3DCF0), Color(0xFF555D7A), koyuluk)
    drawPath(
        path = yol,
        brush = Brush.verticalGradient(
            colors = listOf(ust, alt),
            startY = merkez.y - w * 0.38f,
            endY = merkez.y + w * 0.28f
        )
    )

    // Üstte ışık yansıması
    drawOval(
        color = Color.White.copy(alpha = 0.55f * (1f - koyuluk * 0.6f)),
        topLeft = Offset(merkez.x - w * 0.12f, merkez.y - w * 0.32f),
        size = Size(w * 0.24f, w * 0.10f)
    )
}

private fun DrawScope.damlalar(merkez: Offset, w: Float, t: Float, adet: Int) {
    for (i in 0 until adet) {
        val x = merkez.x + (i - (adet - 1) / 2f) * w * 0.24f
        val faz = (t * 2f + i * 0.33f) % 1f
        val y = merkez.y + faz * w * 0.18f
        val alfa = 1f - faz * 0.7f
        val d = w * 0.06f

        val damla = Path().apply {
            moveTo(x, y - d * 1.7f)
            cubicTo(x + d * 1.2f, y - d * 0.2f, x + d, y + d, x, y + d)
            cubicTo(x - d, y + d, x - d * 1.2f, y - d * 0.2f, x, y - d * 1.7f)
            close()
        }
        drawPath(
            path = damla,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB3E5FC), Color(0xFF1E88E5)),
                startY = y - d * 1.7f,
                endY = y + d
            ),
            alpha = alfa
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.6f * alfa),
            radius = d * 0.25f,
            center = Offset(x - d * 0.35f, y - d * 0.1f)
        )
    }
}

private fun DrawScope.karTaneleri(merkez: Offset, w: Float, t: Float) {
    val konumlar = listOf(-0.26f, 0f, 0.26f)
    konumlar.forEachIndexed { i, dx ->
        val c = Offset(merkez.x + dx * w, merkez.y + (if (i == 1) w * 0.08f else 0f))
        val r = w * 0.08f
        rotate(degrees = t * 120f + i * 20f, pivot = c) {
            for (k in 0 until 3) {
                val aci = k * TAM_TUR / 6f
                drawLine(
                    color = Color(0xFFE1F5FE),
                    start = Offset(c.x - cos(aci) * r, c.y - sin(aci) * r),
                    end = Offset(c.x + cos(aci) * r, c.y + sin(aci) * r),
                    strokeWidth = w * 0.03f,
                    cap = StrokeCap.Round
                )
            }
        }
        drawCircle(color = Color(0xFF81D4FA), radius = w * 0.022f, center = c)
    }
}

private fun DrawScope.simsek(merkez: Offset, w: Float, t: Float) {
    val yol = Path().apply {
        moveTo(merkez.x + w * 0.06f, merkez.y - w * 0.10f)
        lineTo(merkez.x - w * 0.12f, merkez.y + w * 0.14f)
        lineTo(merkez.x - w * 0.01f, merkez.y + w * 0.14f)
        lineTo(merkez.x - w * 0.09f, merkez.y + w * 0.36f)
        lineTo(merkez.x + w * 0.15f, merkez.y + w * 0.06f)
        lineTo(merkez.x + w * 0.03f, merkez.y + w * 0.06f)
        lineTo(merkez.x + w * 0.13f, merkez.y - w * 0.10f)
        close()
    }
    val titreme = if ((t * 4f) % 1f < 0.12f) 0.45f else 1f
    drawPath(
        path = yol,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF176), Color(0xFFFFA000)),
            startY = merkez.y - w * 0.10f,
            endY = merkez.y + w * 0.36f
        ),
        alpha = titreme
    )
}

private fun DrawScope.sisCizgileri(merkez: Offset, s: Float, t: Float) {
    val satirlar = listOf(0f to 0.30f, 0.09f to 0.22f, 0.18f to 0.26f)
    satirlar.forEachIndexed { i, (dy, yariGenislik) ->
        val kayma = sin((t + i * 0.25f) * TAM_TUR) * s * 0.03f
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(merkez.x - yariGenislik * s + kayma, merkez.y + dy * s),
            end = Offset(merkez.x + yariGenislik * s + kayma, merkez.y + dy * s),
            strokeWidth = s * 0.05f,
            cap = StrokeCap.Round
        )
    }
}
