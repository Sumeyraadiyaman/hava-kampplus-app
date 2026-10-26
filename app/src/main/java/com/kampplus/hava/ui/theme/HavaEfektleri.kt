package com.kampplus.hava.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Bir parçacık (yağmur damlası, kar tanesi, yıldız, yaprak...).
 * x ve y ekranın oranı olarak (0..1), boyut ise "derinlik" (0 = uzak, 1 = yakın).
 * Yakındaki parçacıklar daha büyük, daha hızlı ve daha parlak çizilir: 3B derinlik hissi.
 */
private class Parcacik(
    val x: Float,
    val y: Float,
    val boyut: Float,
    val hiz: Float,
    val faz: Float,
    val renk: Color
)

private class EfektVerisi(
    val ana: List<Parcacik> = emptyList(),
    val ek: List<Parcacik> = emptyList(),
    val ek2: List<Parcacik> = emptyList()
)

private val IKI_PI = (2.0 * PI).toFloat()

private fun parcaciklariUret(adet: Int, tohum: Int, renkler: List<Color>): List<Parcacik> {
    val rastgele = Random(tohum)
    return List(adet) {
        Parcacik(
            x = rastgele.nextFloat(),
            y = rastgele.nextFloat(),
            boyut = rastgele.nextFloat(),
            hiz = 0.5f + rastgele.nextFloat(),
            faz = rastgele.nextFloat() * IKI_PI,
            renk = renkler[rastgele.nextInt(renkler.size)]
        )
    }
}

private val beyaz = listOf(Color.White)

private fun efektVerisi(efekt: HavaEfekti): EfektVerisi = when (efekt) {
    HavaEfekti.YOK -> EfektVerisi()
    HavaEfekti.GUNES -> EfektVerisi(ana = parcaciklariUret(25, 1, beyaz))
    HavaEfekti.SICAK -> EfektVerisi(
        ana = parcaciklariUret(
            40, 2,
            listOf(Color.White, Color(0xFFFFE0B2), Color(0xFFFFCC80))
        )
    )
    HavaEfekti.ILKBAHAR -> EfektVerisi(
        ana = parcaciklariUret(
            35, 3,
            listOf(Color(0xFFFFC1D9), Color(0xFFFF8FB1), Color.White, Color(0xFFFFF59D))
        ),
        ek = parcaciklariUret(
            5, 4,
            listOf(Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4FC3F7), Color(0xFFFFEE58))
        ),
        ek2 = parcaciklariUret(4, 5, beyaz)
    )
    HavaEfekti.SONBAHAR -> EfektVerisi(
        ana = parcaciklariUret(
            28, 6,
            listOf(
                Color(0xFFE65100), Color(0xFFD84315), Color(0xFFFFA000),
                Color(0xFF8D6E63), Color(0xFFC62828)
            )
        )
    )
    HavaEfekti.YILDIZ -> EfektVerisi(ana = parcaciklariUret(90, 7, beyaz))
    HavaEfekti.BULUT -> EfektVerisi(ana = parcaciklariUret(6, 8, beyaz))
    HavaEfekti.SIS -> EfektVerisi(ana = parcaciklariUret(6, 9, beyaz))
    HavaEfekti.YAGMUR -> EfektVerisi(
        ana = parcaciklariUret(150, 10, beyaz),
        ek = parcaciklariUret(4, 11, beyaz)
    )
    HavaEfekti.KAR -> EfektVerisi(ana = parcaciklariUret(110, 12, beyaz))
    HavaEfekti.FIRTINA -> EfektVerisi(
        ana = parcaciklariUret(220, 13, beyaz),
        ek = parcaciklariUret(5, 14, beyaz)
    )
}

/** Ekran açık kaldığı sürece saniye cinsinden artan bir zaman değeri üretir. */
@Composable
private fun rememberAnimasyonZamani(): State<Float> {
    val zaman = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val baslangic = withFrameNanos { it }
        while (true) {
            withFrameNanos { an ->
                zaman.floatValue = (an - baslangic) / 1_000_000_000f
            }
        }
    }
    return zaman
}

@Composable
fun HavaEfektKatmani(efekt: HavaEfekti, modifier: Modifier = Modifier) {
    if (efekt == HavaEfekti.YOK) return

    val zaman = rememberAnimasyonZamani()
    val veri = remember(efekt) { efektVerisi(efekt) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Zamanı burada (çizim aşamasında) okuyoruz: her karede sadece çizim yenilenir,
        // ekranın geri kalanı yeniden oluşturulmaz. Bu, animasyonu akıcı tutar.
        val t = zaman.value

        when (efekt) {
            HavaEfekti.YOK -> Unit
            HavaEfekti.GUNES -> {
                gunesCiz(t, sicak = false)
                pariltiCiz(t, veri.ana)
            }
            HavaEfekti.SICAK -> {
                gunesCiz(t, sicak = true)
                buharCiz(t, veri.ana)
            }
            HavaEfekti.ILKBAHAR -> {
                gunesCiz(t, sicak = false)
                yaprakCiz(t, veri.ana)
                kelebekCiz(t, veri.ek)
                bocekCiz(t, veri.ek2)
            }
            HavaEfekti.SONBAHAR -> {
                gunesCiz(t, sicak = false)
                yaprakCiz(t, veri.ana)
            }
            HavaEfekti.YILDIZ -> yildizCiz(t, veri.ana)
            HavaEfekti.BULUT -> bulutCiz(t, veri.ana)
            HavaEfekti.SIS -> sisCiz(t, veri.ana)
            HavaEfekti.YAGMUR -> {
                bulutCiz(t, veri.ek)
                yagmurCiz(t, veri.ana, siddet = 1f)
            }
            HavaEfekti.KAR -> karCiz(t, veri.ana)
            HavaEfekti.FIRTINA -> {
                bulutCiz(t, veri.ek)
                yagmurCiz(t, veri.ana, siddet = 1.4f)
                simsekCiz(t)
            }
        }
    }
}

// ---------------------------------------------------------------- GÜNEŞ

private fun DrawScope.gunesCiz(t: Float, sicak: Boolean) {
    val w = size.width
    val merkez = Offset(w * 0.82f, size.height * 0.1f)
    val nabiz = 1f + sin(t * 1.2f) * 0.04f
    val haleYaricap = w * 0.55f * nabiz

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                (if (sicak) Color(0xFFFFE082) else Color(0xFFFFF59D)).copy(alpha = 0.55f),
                (if (sicak) Color(0xFFFF7043) else Color(0xFFFFE082)).copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = merkez,
            radius = haleYaricap
        ),
        radius = haleYaricap,
        center = merkez
    )

    rotate(degrees = t * 6f, pivot = merkez) {
        for (i in 0 until 12) {
            val aci = i * IKI_PI / 12f
            val ic = w * 0.12f
            val dis = w * (0.42f + 0.05f * sin(t * 2f + i))
            drawLine(
                color = Color.White.copy(alpha = 0.16f),
                start = Offset(merkez.x + cos(aci) * ic, merkez.y + sin(aci) * ic),
                end = Offset(merkez.x + cos(aci) * dis, merkez.y + sin(aci) * dis),
                strokeWidth = 10f * density,
                cap = StrokeCap.Round
            )
        }
    }

    drawCircle(color = Color.White.copy(alpha = 0.95f), radius = w * 0.07f, center = merkez)

    // Mercek parlaması: güneşten ekranın ortasına doğru dizilen soluk halkalar (3B his)
    val yonX = w * 0.5f - merkez.x
    val yonY = size.height * 0.5f - merkez.y
    val halkalar = listOf(0.6f to 0.05f, 1.1f to 0.03f, 1.5f to 0.07f)
    for ((oran, buyukluk) in halkalar) {
        drawCircle(
            color = Color.White.copy(alpha = 0.07f),
            radius = w * buyukluk,
            center = Offset(merkez.x + yonX * oran, merkez.y + yonY * oran)
        )
    }
}

private fun DrawScope.pariltiCiz(t: Float, zerreler: List<Parcacik>) {
    for (z in zerreler) {
        val x = z.x * size.width + sin(t * 0.3f + z.faz) * 20f * density
        val ham = (z.y - t * 0.01f * z.hiz) % 1f
        val y = ((ham + 1f) % 1f) * size.height
        val alfa = 0.15f + 0.35f * ((sin(t * 2f * z.hiz + z.faz) + 1f) / 2f)
        drawCircle(
            color = Color.White.copy(alpha = alfa),
            radius = (1.5f + z.boyut * 2.5f) * density,
            center = Offset(x, y)
        )
    }
}

// ---------------------------------------------------------------- SICAK: BUHAR

private fun DrawScope.buharCiz(t: Float, buharlar: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (b in buharlar) {
        val hiz = 0.03f + b.hiz * 0.04f
        val ilerleme = (b.y + t * hiz) % 1f
        val y = h * (1.05f - ilerleme * 1.1f)
        val x = b.x * w + sin(t * 0.7f + b.faz) * 30f * density
        val r = (10f + b.boyut * 26f) * density * (0.6f + ilerleme)
        val alfa = sin(ilerleme * PI.toFloat()) * (0.10f + b.boyut * 0.12f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(b.renk.copy(alpha = alfa), Color.Transparent),
                center = Offset(x, y),
                radius = r
            ),
            radius = r,
            center = Offset(x, y)
        )
    }

    // Sıcaktan titreyen hava: ekranın alt kısmında dalgalanan ince şeritler
    for (i in 0 until 6) {
        val y = h * (0.62f + i * 0.06f)
        val yol = Path()
        yol.moveTo(0f, y)
        var x = 0f
        while (x <= w) {
            yol.lineTo(x, y + sin(x / (40f * density) + t * 3f + i) * 4f * density)
            x += 12f * density
        }
        drawPath(
            path = yol,
            color = Color.White.copy(alpha = 0.06f),
            style = Stroke(width = 2f * density)
        )
    }
}

// ---------------------------------------------------------------- YAPRAK / ÇİÇEK

private fun DrawScope.yaprakCiz(t: Float, yapraklar: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (p in yapraklar) {
        val boy = (6f + p.boyut * 10f) * density
        val hiz = 0.05f + p.hiz * 0.05f
        val y = ((p.y + t * hiz) % 1f) * (h + boy * 4f) - boy * 2f
        val x = p.x * w + sin(t * 0.9f + p.faz) * 40f * density
        val donus = t * (40f + p.hiz * 60f) + p.faz * 57f
        // Yaprağın kendi ekseninde dönerken incelip kalınlaşması: 3B dönme hissi
        val yassilik = 0.35f + 0.65f * abs(sin(t * 1.5f + p.faz))

        rotate(degrees = donus, pivot = Offset(x, y)) {
            drawOval(
                color = p.renk.copy(alpha = 0.85f),
                topLeft = Offset(x - boy / 2f, y - boy * yassilik / 2f),
                size = Size(boy, boy * yassilik)
            )
        }
    }
}

private fun DrawScope.kelebekCiz(t: Float, kelebekler: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (k in kelebekler) {
        val merkez = Offset(
            x = w * (0.15f + 0.7f * ((sin(t * 0.25f * k.hiz + k.faz) + 1f) / 2f)),
            y = h * (0.25f + 0.45f * ((cos(t * 0.33f * k.hiz + k.faz * 1.3f) + 1f) / 2f)) +
                sin(t * 3f + k.faz) * 8f * density
        )
        val kanat = (7f + k.boyut * 6f) * density
        val cirpma = 0.25f + 0.75f * abs(sin(t * 9f + k.faz))

        for (yon in listOf(-1f, 1f)) {
            val ustX = if (yon < 0f) merkez.x - kanat * cirpma else merkez.x
            val altX = if (yon < 0f) merkez.x - kanat * 0.8f * cirpma else merkez.x
            drawOval(
                color = k.renk.copy(alpha = 0.9f),
                topLeft = Offset(ustX, merkez.y - kanat),
                size = Size(kanat * cirpma, kanat)
            )
            drawOval(
                color = k.renk.copy(alpha = 0.7f),
                topLeft = Offset(altX, merkez.y - kanat * 0.1f),
                size = Size(kanat * 0.8f * cirpma, kanat * 0.8f)
            )
        }
        drawLine(
            color = Color(0xFF3E2723),
            start = Offset(merkez.x, merkez.y - kanat * 0.9f),
            end = Offset(merkez.x, merkez.y + kanat * 0.7f),
            strokeWidth = 1.5f * density,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.bocekCiz(t: Float, bocekler: List<Parcacik>) {
    for (b in bocekler) {
        val x = size.width * b.x +
            sin(t * 4f * b.hiz + b.faz) * 25f * density +
            cos(t * 7f + b.faz) * 6f * density
        val y = size.height * (0.5f + b.y * 0.35f) + cos(t * 5f * b.hiz + b.faz) * 15f * density
        drawCircle(color = Color(0xFFFFD54F), radius = 3f * density, center = Offset(x, y))
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.5f * density,
            center = Offset(x + 2f * density, y - 3f * density)
        )
    }
}

// ---------------------------------------------------------------- GECE

private fun DrawScope.yildizCiz(t: Float, yildizlar: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (s in yildizlar) {
        val parlaklik = 0.25f + 0.75f * ((sin(t * (0.8f + s.hiz * 1.5f) + s.faz) + 1f) / 2f)
        val r = (0.6f + s.boyut * 1.6f) * density
        val merkez = Offset(s.x * w, s.y * h * 0.7f)
        drawCircle(color = Color.White.copy(alpha = parlaklik), radius = r, center = merkez)
        if (s.boyut > 0.85f) {
            drawCircle(color = Color.White.copy(alpha = parlaklik * 0.25f), radius = r * 4f, center = merkez)
        }
    }

    // Kayan yıldız: her 7 saniyede bir
    val dongu = 7f
    val ilerleme = (t % dongu) / 1.2f
    if (ilerleme < 1f) {
        val tur = (t / dongu).toInt()
        val rastgele = Random(tur)
        val baslangicX = w * (0.2f + rastgele.nextFloat() * 0.6f)
        val baslangicY = h * (0.05f + rastgele.nextFloat() * 0.25f)
        val bas = Offset(baslangicX + ilerleme * w * 0.35f, baslangicY + ilerleme * h * 0.12f)
        val kuyruk = Offset(bas.x - w * 0.12f, bas.y - h * 0.04f)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f * (1f - ilerleme))),
                start = kuyruk,
                end = bas
            ),
            start = kuyruk,
            end = bas,
            strokeWidth = 2f * density,
            cap = StrokeCap.Round
        )
    }
}

// ---------------------------------------------------------------- BULUT / SİS

private fun DrawScope.bulutCiz(t: Float, bulutlar: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (b in bulutlar) {
        val olcek = (0.6f + b.boyut * 0.9f) * w * 0.22f
        // Yakındaki bulutlar daha hızlı kayar (paralaks): derinlik hissi
        val hiz = 0.008f + b.boyut * 0.02f
        val x = ((b.x + t * hiz) % 1.4f) * (w + olcek * 4f) - olcek * 2f
        val y = h * (0.05f + b.y * 0.45f)
        val renk = Color.White.copy(alpha = 0.10f + b.boyut * 0.14f)

        drawCircle(color = renk, radius = olcek * 0.55f, center = Offset(x, y))
        drawCircle(color = renk, radius = olcek * 0.45f, center = Offset(x - olcek * 0.55f, y + olcek * 0.12f))
        drawCircle(color = renk, radius = olcek * 0.42f, center = Offset(x + olcek * 0.55f, y + olcek * 0.15f))
        drawCircle(color = renk, radius = olcek * 0.35f, center = Offset(x + olcek * 0.2f, y - olcek * 0.3f))
        drawOval(color = renk, topLeft = Offset(x - olcek, y), size = Size(olcek * 2f, olcek * 0.5f))
    }
}

private fun DrawScope.sisCiz(t: Float, katmanlar: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (k in katmanlar) {
        val genislik = w * (1.2f + k.boyut)
        val yukseklik = h * (0.08f + k.boyut * 0.08f)
        val x = -w * 0.3f + sin(t * 0.08f * k.hiz + k.faz) * w * 0.25f
        val y = h * (0.15f + k.y * 0.75f)
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.16f + k.boyut * 0.1f),
                    Color.Transparent
                ),
                startY = y,
                endY = y + yukseklik
            ),
            topLeft = Offset(x, y),
            size = Size(genislik, yukseklik)
        )
    }
}

// ---------------------------------------------------------------- YAĞMUR / KAR / ŞİMŞEK

private fun DrawScope.yagmurCiz(t: Float, damlalar: List<Parcacik>, siddet: Float) {
    val w = size.width
    val h = size.height

    for (d in damlalar) {
        val uzunluk = (10f + d.boyut * 18f) * density * siddet
        val hiz = (0.9f + d.boyut * 1.4f) * siddet
        val yol = h + uzunluk * 2f
        val y = ((d.y + t * hiz) % 1f) * yol - uzunluk
        val x = d.x * (w + 200f) - 100f - y * 0.12f
        drawLine(
            color = Color.White.copy(alpha = 0.15f + d.boyut * 0.35f),
            start = Offset(x, y),
            end = Offset(x - uzunluk * 0.12f, y + uzunluk),
            strokeWidth = (0.8f + d.boyut * 1.2f) * density,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.karCiz(t: Float, taneler: List<Parcacik>) {
    val w = size.width
    val h = size.height

    for (k in taneler) {
        val yaricap = (1.5f + k.boyut * 3.5f) * density
        val hiz = 0.04f + k.boyut * 0.08f
        val y = ((k.y + t * hiz) % 1f) * (h + yaricap * 4f) - yaricap * 2f
        val x = k.x * w + sin(t * (0.6f + k.hiz * 0.6f) + k.faz) * 18f * density * (0.5f + k.boyut)

        if (k.boyut > 0.8f) {
            // Yakındaki iri taneler: dönen altı kollu kar kristali
            rotate(degrees = t * 40f * k.hiz + k.faz * 57f, pivot = Offset(x, y)) {
                for (i in 0 until 3) {
                    val aci = i * PI.toFloat() / 3f
                    val dx = cos(aci) * yaricap * 2.2f
                    val dy = sin(aci) * yaricap * 2.2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(x - dx, y - dy),
                        end = Offset(x + dx, y + dy),
                        strokeWidth = 1.2f * density,
                        cap = StrokeCap.Round
                    )
                }
            }
        } else {
            drawCircle(
                color = Color.White.copy(alpha = 0.45f + k.boyut * 0.5f),
                radius = yaricap,
                center = Offset(x, y)
            )
        }
    }

    // Altta biriken kar / kırağı
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.28f)),
            startY = h * 0.78f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.78f),
        size = Size(w, h * 0.22f)
    )
    // Sol üst köşede buz parıltısı
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
            center = Offset.Zero,
            radius = w * 0.6f
        ),
        radius = w * 0.6f,
        center = Offset.Zero
    )
}

private fun DrawScope.simsekCiz(t: Float) {
    val dongu = 5.5f
    val tur = (t / dongu).toInt()
    val an = t % dongu
    val flas = when {
        an < 0.08f -> 1f
        an < 0.16f -> 0.2f
        an < 0.24f -> 0.8f
        an < 0.6f -> (0.6f - an) / 0.36f * 0.3f
        else -> 0f
    }
    if (flas <= 0f) return

    drawRect(color = Color.White.copy(alpha = flas * 0.35f))

    if (an < 0.3f) {
        val rastgele = Random(tur)
        val yol = Path()
        var x = size.width * (0.2f + rastgele.nextFloat() * 0.6f)
        var y = 0f
        yol.moveTo(x, y)
        while (y < size.height * 0.55f) {
            x += (rastgele.nextFloat() - 0.5f) * 60f * density
            y += (20f + rastgele.nextFloat() * 30f) * density
            yol.lineTo(x, y)
        }
        drawPath(
            path = yol,
            color = Color.White.copy(alpha = 0.35f * flas),
            style = Stroke(width = 10f * density, cap = StrokeCap.Round)
        )
        drawPath(
            path = yol,
            color = Color.White.copy(alpha = flas),
            style = Stroke(width = 3f * density, cap = StrokeCap.Round)
        )
    }
}
