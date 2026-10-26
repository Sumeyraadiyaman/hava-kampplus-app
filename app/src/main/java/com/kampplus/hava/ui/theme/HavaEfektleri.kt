package com.kampplus.hava.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Arka plandaki hareketli hava efektleri.
 * Her parçacık (yağmur damlası, kar tanesi, yıldız...) rastgele ama sabit bir
 * başlangıç değeriyle oluşturulur. Konumu zamana göre hesaplanır, böylece
 * parçacık listesini her karede güncellemek gerekmez.
 */
private class Parcacik(
    val x: Float,
    val y: Float,
    val hiz: Float,
    val boyut: Float,
    val faz: Float
)

@Composable
fun HavaEfektKatmani(efekt: HavaEfekti, modifier: Modifier = Modifier) {
    if (efekt == HavaEfekti.YOK) return

    val zaman by produceState(initialValue = 0f, efekt) {
        val baslangic = withFrameNanos { an -> an }
        while (true) {
            withFrameNanos { an -> value = (an - baslangic) / 1_000_000_000f }
        }
    }
    val parcaciklar = remember(efekt) { parcacikUret(efekt) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = zaman
        when (efekt) {
            HavaEfekti.GUNES -> gunesCiz(t, Color(0xFFFFF59D), Color(0xFFFFE082))
            HavaEfekti.SICAK -> {
                gunesCiz(t, Color(0xFFFFF3E0), Color(0xFFFFAB40))
                buharCiz(parcaciklar, t)
            }
            HavaEfekti.CICEK -> {
                gunesCiz(t, Color(0xFFFFF9C4), Color(0xFFFFF59D))
                cicekCiz(parcaciklar.drop(5), t)
                kelebekCiz(parcaciklar.take(5), t)
            }
            HavaEfekti.YILDIZ -> {
                ayCiz(t)
                yildizCiz(parcaciklar, t)
            }
            HavaEfekti.BULUT -> bulutCiz(parcaciklar, t, Color.White.copy(alpha = 0.22f), 1f)
            HavaEfekti.SIS -> sisCiz(t)
            HavaEfekti.YAGMUR -> {
                bulutCiz(parcaciklar.take(4), t, Color.White.copy(alpha = 0.10f), 0.02f)
                yagmurCiz(parcaciklar, t)
            }
            HavaEfekti.KAR -> {
                karCiz(parcaciklar, t)
                buzCiz()
            }
            HavaEfekti.FIRTINA -> {
                bulutCiz(parcaciklar.take(5), t, Color(0xFF0D0E1A).copy(alpha = 0.35f), 0.03f)
                yagmurCiz(parcaciklar, t)
                simsekCiz(t)
            }
            HavaEfekti.YOK -> Unit
        }
    }
}

private fun parcacikUret(efekt: HavaEfekti): List<Parcacik> {
    val rastgele = Random(efekt.ordinal * 97 + 13)
    val adet = when (efekt) {
        HavaEfekti.YAGMUR -> 110
        HavaEfekti.FIRTINA -> 150
        HavaEfekti.KAR -> 80
        HavaEfekti.YILDIZ -> 90
        HavaEfekti.CICEK -> 34
        HavaEfekti.SICAK -> 16
        HavaEfekti.BULUT -> 6
        else -> 0
    }
    return List(adet) {
        val hiz = when (efekt) {
            HavaEfekti.YAGMUR, HavaEfekti.FIRTINA -> 0.9f + rastgele.nextFloat() * 0.7f
            HavaEfekti.KAR -> 0.04f + rastgele.nextFloat() * 0.08f
            HavaEfekti.CICEK -> 0.03f + rastgele.nextFloat() * 0.05f
            HavaEfekti.SICAK -> 0.04f + rastgele.nextFloat() * 0.05f
            HavaEfekti.BULUT -> 0.008f + rastgele.nextFloat() * 0.015f
            else -> 0.5f + rastgele.nextFloat()
        }
        Parcacik(
            x = rastgele.nextFloat(),
            y = rastgele.nextFloat(),
            hiz = hiz,
            boyut = rastgele.nextFloat(),
            faz = rastgele.nextFloat() * 6.2832f
        )
    }
}

// ---------------- Güneş ve ay ----------------

private fun DrawScope.gunesCiz(t: Float, cekirdek: Color, hale: Color) {
    val merkez = Offset(size.width * 0.18f, size.height * 0.11f)
    val yaricap = 34.dp.toPx()
    val nabiz = 1f + 0.06f * sin(t * 1.6f)
    val haleYaricapi = yaricap * 3.4f * nabiz

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(hale.copy(alpha = 0.6f), hale.copy(alpha = 0.15f), Color.Transparent),
            center = merkez,
            radius = haleYaricapi
        ),
        radius = haleYaricapi,
        center = merkez
    )

    rotate(degrees = t * 10f, pivot = merkez) {
        for (i in 0 until 12) {
            val aci = i * (Math.PI / 6).toFloat()
            val uzunluk = yaricap * (2.0f + 0.35f * sin(t * 2.2f + i))
            val bas = Offset(merkez.x + cos(aci) * yaricap * 1.35f, merkez.y + sin(aci) * yaricap * 1.35f)
            val son = Offset(merkez.x + cos(aci) * uzunluk, merkez.y + sin(aci) * uzunluk)
            drawLine(
                color = hale.copy(alpha = 0.45f),
                start = bas,
                end = son,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    drawCircle(color = cekirdek, radius = yaricap, center = merkez)
}

private fun DrawScope.ayCiz(t: Float) {
    val merkez = Offset(size.width * 0.2f, size.height * 0.12f)
    val yaricap = 26.dp.toPx()
    val hale = yaricap * (2.6f + 0.15f * sin(t * 0.8f))

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF8E1).copy(alpha = 0.35f), Color.Transparent),
            center = merkez,
            radius = hale
        ),
        radius = hale,
        center = merkez
    )
    drawCircle(color = Color(0xFFFFF3D1), radius = yaricap, center = merkez)
    drawCircle(
        color = Color(0xFFE9DDBA),
        radius = yaricap * 0.22f,
        center = merkez + Offset(-yaricap * 0.3f, -yaricap * 0.2f)
    )
    drawCircle(
        color = Color(0xFFE9DDBA),
        radius = yaricap * 0.14f,
        center = merkez + Offset(yaricap * 0.35f, yaricap * 0.3f)
    )
}

// ---------------- Gece ----------------

private fun DrawScope.yildizCiz(yildizlar: List<Parcacik>, t: Float) {
    for (s in yildizlar) {
        val parlaklik = 0.25f + 0.75f * (0.5f + 0.5f * sin(t * (1f + s.hiz * 2f) + s.faz))
        val yaricap = (0.8f + s.boyut * 1.8f).dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = parlaklik),
            radius = yaricap,
            center = Offset(s.x * size.width, s.y * size.height * 0.65f)
        )
    }

    // Arada bir kayan yıldız
    val dongu = 9f
    val faz = t % dongu
    if (faz < 1.2f) {
        val r = Random((t / dongu).toInt())
        val basX = size.width * (0.3f + r.nextFloat() * 0.6f)
        val basY = size.height * (0.05f + r.nextFloat() * 0.2f)
        val ilerleme = faz / 1.2f
        val uc = Offset(basX - ilerleme * size.width * 0.35f, basY + ilerleme * size.height * 0.15f)
        val kuyruk = Offset(uc.x + size.width * 0.08f, uc.y - size.height * 0.035f)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f * (1f - ilerleme)), Color.Transparent),
                start = uc,
                end = kuyruk
            ),
            start = uc,
            end = kuyruk,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// ---------------- Bulut ve sis ----------------

private fun DrawScope.bulutCiz(bulutlar: List<Parcacik>, t: Float, renk: Color, hizCarpani: Float) {
    for (b in bulutlar) {
        val genislik = size.width * (0.35f + b.boyut * 0.3f)
        val ilerleme = (b.x + t * b.hiz * hizCarpani) % 1f
        val x = ilerleme * (size.width + genislik * 2f) - genislik
        val y = size.height * (0.04f + b.y * 0.32f)

        // Parçaları tek bir yol olarak çiziyoruz ki üst üste binen yerler koyulaşmasın
        val yol = Path().apply {
            addOval(Rect(center = Offset(x + genislik * 0.25f, y + genislik * 0.06f), radius = genislik * 0.2f))
            addOval(Rect(center = Offset(x + genislik * 0.5f, y - genislik * 0.04f), radius = genislik * 0.28f))
            addOval(Rect(center = Offset(x + genislik * 0.76f, y + genislik * 0.07f), radius = genislik * 0.19f))
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(x + genislik * 0.12f, y + genislik * 0.04f),
                        size = Size(genislik * 0.76f, genislik * 0.2f)
                    ),
                    cornerRadius = CornerRadius(genislik * 0.1f)
                )
            )
        }
        drawPath(path = yol, color = renk)
    }
}

private fun DrawScope.sisCiz(t: Float) {
    for (i in 0 until 8) {
        val ilerleme = ((i * 0.17f + t * 0.012f * (1 + i % 3)) % 1.3f) - 0.15f
        val merkez = Offset(ilerleme * size.width, size.height * (0.15f + (i % 4) * 0.2f))
        val yaricap = size.width * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                center = merkez,
                radius = yaricap
            ),
            radius = yaricap,
            center = merkez
        )
    }
}

// ---------------- Yağmur, kar, fırtına ----------------

private fun DrawScope.yagmurCiz(damlalar: List<Parcacik>, t: Float) {
    val genislik = size.width
    val yukseklik = size.height
    val egim = 0.18f
    for (d in damlalar) {
        val ilerleme = (d.y + t * d.hiz) % 1.15f
        val konumY = ilerleme * yukseklik - 0.08f * yukseklik
        val konumX = ((d.x * genislik + konumY * egim) % genislik + genislik) % genislik
        val uzunluk = (14f + d.boyut * 18f).dp.toPx()
        drawLine(
            color = Color.White.copy(alpha = 0.25f + d.boyut * 0.35f),
            start = Offset(konumX, konumY),
            end = Offset(konumX - uzunluk * egim, konumY - uzunluk),
            strokeWidth = (1f + d.boyut * 1.2f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.karCiz(taneler: List<Parcacik>, t: Float) {
    for (k in taneler) {
        val ilerleme = (k.y + t * k.hiz) % 1.1f
        val konumY = ilerleme * size.height - 0.05f * size.height
        val salinim = sin(t * (0.8f + k.boyut) + k.faz) * (10f + k.boyut * 18f).dp.toPx()
        val konumX = k.x * size.width + salinim
        val yaricap = (1.5f + k.boyut * 3.5f).dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = 0.5f + k.boyut * 0.5f),
            radius = yaricap,
            center = Offset(konumX, konumY)
        )
    }
}

private fun DrawScope.buzCiz() {
    val baslangic = size.height * 0.75f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
            startY = baslangic,
            endY = size.height
        ),
        topLeft = Offset(0f, baslangic),
        size = Size(size.width, size.height - baslangic)
    )
}

private fun DrawScope.simsekCiz(t: Float) {
    val dongu = 5.5f
    val faz = t % dongu
    val parlaklik = when {
        faz < 0.08f -> 0.55f
        faz in 0.16f..0.24f -> 0.35f
        else -> 0f
    }
    if (parlaklik <= 0f) return

    drawRect(color = Color.White.copy(alpha = parlaklik * 0.6f))

    val r = Random((t / dongu).toInt())
    val yol = Path()
    var x = size.width * (0.2f + r.nextFloat() * 0.6f)
    var y = 0f
    yol.moveTo(x, y)
    while (y < size.height * 0.55f) {
        x += (r.nextFloat() - 0.5f) * 80.dp.toPx() / 2f
        y += (20f + r.nextFloat() * 25f).dp.toPx()
        yol.lineTo(x, y)
    }
    drawPath(
        path = yol,
        color = Color.White.copy(alpha = (parlaklik + 0.3f).coerceAtMost(1f)),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// ---------------- Sıcak hava ----------------

private fun DrawScope.buharCiz(kabarciklar: List<Parcacik>, t: Float) {
    for (b in kabarciklar) {
        val ilerleme = (b.y + t * b.hiz) % 1.2f
        val merkez = Offset(
            b.x * size.width + sin(t * 0.7f + b.faz) * 28.dp.toPx(),
            size.height * (1.1f - ilerleme)
        )
        val yaricap = (36f + b.boyut * 60f).dp.toPx() * (0.6f + ilerleme * 0.5f)
        val alfa = (0.16f * (1f - ilerleme / 1.2f)).coerceAtLeast(0f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = alfa), Color.Transparent),
                center = merkez,
                radius = yaricap
            ),
            radius = yaricap,
            center = merkez
        )
    }

    // Isı dalgası çizgileri
    for (i in 0 until 5) {
        val yol = Path()
        val tabanY = size.height * (0.55f + i * 0.09f)
        val adim = size.width / 24f
        yol.moveTo(0f, tabanY)
        var x = 0f
        while (x <= size.width) {
            val dalga = sin(x / size.width * 12f + t * 2f + i) * 6.dp.toPx()
            yol.lineTo(x, tabanY + dalga)
            x += adim
        }
        drawPath(path = yol, color = Color.White.copy(alpha = 0.07f), style = Stroke(width = 2.dp.toPx()))
    }
}

// ---------------- İlkbahar ----------------

private val cicekRenkleri = listOf(
    Color(0xFFFFC1E3), Color(0xFFFFFFFF), Color(0xFFFFE082), Color(0xFFF8BBD0)
)

private val kelebekRenkleri = listOf(
    Color(0xFFFF8A65), Color(0xFFBA68C8), Color(0xFFFFD54F), Color(0xFF4FC3F7), Color(0xFFF06292)
)

private fun DrawScope.cicekCiz(yapraklar: List<Parcacik>, t: Float) {
    yapraklar.forEachIndexed { i, yaprak ->
        val ilerleme = (yaprak.y + t * yaprak.hiz) % 1.1f
        val konum = Offset(
            yaprak.x * size.width + sin(t * 0.9f + yaprak.faz) * 30.dp.toPx(),
            ilerleme * size.height - 0.05f * size.height
        )
        val boy = (6f + yaprak.boyut * 6f).dp.toPx()
        val yon = if (i % 2 == 0) 1f else -1f
        rotate(degrees = t * 60f * yon + yaprak.faz * 57f, pivot = konum) {
            drawOval(
                color = cicekRenkleri[i % cicekRenkleri.size].copy(alpha = 0.85f),
                topLeft = Offset(konum.x - boy, konum.y - boy * 0.55f),
                size = Size(boy * 2f, boy * 1.1f)
            )
        }
    }
}

private fun DrawScope.kelebekCiz(kelebekler: List<Parcacik>, t: Float) {
    kelebekler.forEachIndexed { i, k ->
        val hizCarpani = 0.4f + k.hiz * 8f
        val x = size.width * (0.5f + 0.4f * sin(t * 0.33f * hizCarpani + k.faz))
        val y = size.height * (0.32f + 0.22f * sin(t * 0.47f * hizCarpani + k.faz * 1.7f))
        val kanat = 0.3f + 0.7f * abs(sin(t * 9f + k.faz))
        val boy = (9f + k.boyut * 6f).dp.toPx()
        val renk = kelebekRenkleri[i % kelebekRenkleri.size]

        drawOval(
            color = renk,
            topLeft = Offset(x - boy * kanat, y - boy * 0.6f),
            size = Size(boy * kanat, boy * 1.2f)
        )
        drawOval(
            color = renk,
            topLeft = Offset(x, y - boy * 0.6f),
            size = Size(boy * kanat, boy * 1.2f)
        )
        drawLine(
            color = Color(0xFF3E2723),
            start = Offset(x, y - boy * 0.5f),
            end = Offset(x, y + boy * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
