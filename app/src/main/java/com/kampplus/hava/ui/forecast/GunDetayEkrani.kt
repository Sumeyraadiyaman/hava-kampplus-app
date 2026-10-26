package com.kampplus.hava.ui.forecast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.HavaKalitesiSeviyesi
import com.kampplus.hava.model.HourlyWeather
import com.kampplus.hava.model.SporUygunlugu
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.gunlukNotlar
import com.kampplus.hava.model.sporUygunlugu
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.CamKart
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.havaGorunumu
import com.kampplus.hava.ui.theme.tarihtenAy
import com.kampplus.hava.ui.theme.varsayilanGorunum
import kotlin.math.roundToInt

@Composable
fun GunDetayEkrani(
    tarih: String,
    onGeriClick: () -> Unit,
    viewModel: ForecastViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val forecast = (uiState.sonuc as? LoadState.Success)?.data
    val gun = forecast?.daily?.find { gunluk -> gunluk.date == tarih }

    val gorunum = if (gun != null) {
        havaGorunumu(
            kosul = WeatherCondition.fromWeatherCode(gun.weatherCode),
            gunduz = true,
            sicaklik = gun.tempMax,
            ay = tarihtenAy(gun.date)
        )
    } else {
        varsayilanGorunum
    }

    HavaArkaPlani(gorunum = gorunum, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            CamDugme(
                yazi = "← Geri",
                onClick = onGeriClick,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (forecast == null || gun == null) {
                Text(text = "Bu gün için veri bulunamadı")
            } else {
                val saatler = forecast.hourly.filter { saat -> saat.time.startsWith(tarih) }
                GunDetayIcerigi(
                    sehirAdi = uiState.sehirAdi,
                    gun = gun,
                    saatler = saatler,
                    havaKalitesi = forecast.airQuality?.dailyMax?.get(tarih),
                    vurgu = gorunum.vurgu
                )
            }
        }
    }
}

@Composable
fun GunDetayIcerigi(
    sehirAdi: String,
    gun: DailyWeather,
    saatler: List<HourlyWeather>,
    havaKalitesi: Int? = null,
    vurgu: Color = Color(0xFF1565C0)
) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    val ortalamaNem = if (saatler.isEmpty()) "-"
    else "%${saatler.map { saat -> saat.humidity }.average().roundToInt()}"

    val ortalamaBasinc = if (saatler.isEmpty()) "-"
    else "${saatler.map { saat -> saat.pressure }.average().roundToInt()}"

    val gecis = rememberInfiniteTransition(label = "detayEmoji")
    val yukseklik by gecis.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yukseklik"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tarihYaz(gun.date),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "📍 $sehirAdi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = durum.emoji,
                    fontSize = 72.sp,
                    modifier = Modifier.graphicsLayer { translationY = yukseklik.dp.toPx() }
                )
                Text(
                    text = "${gun.tempMax.roundToInt()}° / ${gun.tempMin.roundToInt()}°",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light)
                )
                Text(text = durum.label, style = MaterialTheme.typography.titleLarge)
            }
        }

        item {
            GiyimKarti(
                notlar = gunlukNotlar(gun),
                baslik = "👕 Ne giymeli, nelere dikkat etmeli?",
                vurgu = vurgu
            )
        }

        item {
            SporKarti(spor = sporUygunlugu(gun, saatler, havaKalitesi))
        }

        if (saatler.isNotEmpty()) {
            item {
                SaatlikSerit(saatler = saatler, baslik = "🕐 Saat saat", ilkSaatSimdi = false)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🔎 Detaylar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "🌧️ Yağış ihtimali",
                        deger = "%${gun.precipitationProbability}",
                        alt = "${ondalik(gun.precipitationSum)} mm yağış",
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "💨 En yüksek rüzgâr",
                        deger = "${gun.windSpeedMax.roundToInt()} km/sa",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "💧 Ortalama nem",
                        deger = ortalamaNem,
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "🌡️ Ortalama basınç",
                        deger = ortalamaBasinc,
                        alt = "hPa",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "☀️ UV indeksi",
                        deger = "${gun.uvIndexMax.roundToInt()}",
                        alt = uvSeviyesi(gun.uvIndexMax),
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "⏱️ Gün uzunluğu",
                        deger = gunUzunlugu(gun.sunrise, gun.sunset),
                        alt = "🌅 ${saatKismi(gun.sunrise)}  🌇 ${saatKismi(gun.sunset)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (havaKalitesi != null) {
                    val seviye = HavaKalitesiSeviyesi.fromAqi(havaKalitesi)
                    BilgiKutusu(
                        baslik = "🌫️ Hava kalitesi",
                        deger = "$havaKalitesi",
                        alt = "${seviye.emoji} ${seviye.etiket} (günün en kötü saati)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Spor puanını dolan bir halka ile gösteren kart. */
@Composable
fun SporKarti(spor: SporUygunlugu) {
    val halkaRengi = when {
        spor.puan >= 8 -> Color(0xFF69F0AE)
        spor.puan >= 5 -> Color(0xFFFFD740)
        else -> Color(0xFFFF6E40)
    }

    val ilerleme = remember { Animatable(0f) }
    LaunchedEffect(spor.puan) {
        ilerleme.animateTo(spor.puan / 10f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    CamKart {
        Text(
            text = "🏃 Spor uygunluğu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val kalinlik = 8.dp.toPx()
                    val ust = Offset(kalinlik / 2f, kalinlik / 2f)
                    val boyut = Size(size.width - kalinlik, size.height - kalinlik)
                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = ust,
                        size = boyut,
                        style = Stroke(width = kalinlik, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = halkaRengi,
                        startAngle = -90f,
                        sweepAngle = 360f * ilerleme.value,
                        useCenter = false,
                        topLeft = ust,
                        size = boyut,
                        style = Stroke(width = kalinlik, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${spor.puan}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spor.baslik,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = spor.aciklama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                if (spor.enUygunSaat != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⏰ En uygun saat: ${spor.enUygunSaat}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
