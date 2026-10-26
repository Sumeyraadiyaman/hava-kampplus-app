package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.HavaKalitesiSeviyesi
import com.kampplus.hava.model.HourlyWeather
import com.kampplus.hava.model.SporUygunlugu
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.tumOneriler
import com.kampplus.hava.model.sporUygunlugu
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.AcilirBolum
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.HataGorunumu
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.HavaIkonu
import com.kampplus.hava.ui.theme.OneriKaydirici
import com.kampplus.hava.ui.theme.ayBul
import com.kampplus.hava.ui.theme.camKart
import com.kampplus.hava.ui.theme.havaGorunumu
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
            ay = ayBul(gun.date),
            sicaklik = gun.tempMax
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                CamDugme(metin = "← Geri", onClick = onGeriClick)
            }

            if (forecast == null || gun == null) {
                HataGorunumu(
                    mesaj = "Bu gün için veri bulunamadı.",
                    dugmeMetni = "Geri dön",
                    onClick = onGeriClick
                )
            } else {
                val saatler = forecast.hourly.filter { saat -> saat.time.startsWith(tarih) }
                GunDetayIcerigi(
                    sehirAdi = uiState.sehirAdi,
                    gun = gun,
                    saatler = saatler,
                    havaKalitesi = forecast.airQuality?.dailyMax?.get(tarih)
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
    havaKalitesi: Int? = null
) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    val ortalamaNem = if (saatler.isEmpty()) "-"
    else "%${saatler.map { saat -> saat.humidity }.average().roundToInt()}"

    val ortalamaBasinc = if (saatler.isEmpty()) "-"
    else "${saatler.map { saat -> saat.pressure }.average().roundToInt()}"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tarihYaz(gun.date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "📍 $sehirAdi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                HavaIkonu(kosul = durum, modifier = Modifier.size(120.dp))
                Text(
                    text = "${gun.tempMax.roundToInt()}° / ${gun.tempMin.roundToInt()}°",
                    fontSize = 48.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = durum.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            OneriKaydirici(
                oneriler = tumOneriler(gun),
                baslik = "👕 Ne giymelisin?"
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
            AcilirBolum(baslik = "📊 Detaylar", baslangictaAcik = true) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "🌧️ Yağış ihtimali",
                        deger = "%${gun.precipitationProbability}",
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "☔ Yağış miktarı",
                        deger = "${ondalik(gun.precipitationSum)} mm",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "💨 En yüksek rüzgâr",
                        deger = "${gun.windSpeedMax.roundToInt()} km/sa",
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "☀️ UV",
                        deger = "${gun.uvIndexMax.roundToInt()}",
                        modifier = Modifier.weight(1f),
                        alt = uvSeviyesi(gun.uvIndexMax)
                    )
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
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
                        modifier = Modifier.weight(1f),
                        alt = "hPa"
                    )
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilgiKutusu(
                        baslik = "🌅 Gün doğumu",
                        deger = saatKismi(gun.sunrise),
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "🌇 Gün batımı",
                        deger = saatKismi(gun.sunset),
                        modifier = Modifier.weight(1f)
                    )
                }

                BilgiKutusu(
                    baslik = "⏱️ Gün uzunluğu",
                    deger = gunUzunlugu(gun.sunrise, gun.sunset),
                    modifier = Modifier.fillMaxWidth()
                )

                if (havaKalitesi != null) {
                    val seviye = HavaKalitesiSeviyesi.fromAqi(havaKalitesi)
                    BilgiKutusu(
                        baslik = "🌫️ Hava kalitesi",
                        deger = "$havaKalitesi · ${seviye.etiket}",
                        modifier = Modifier.fillMaxWidth(),
                        alt = "${seviye.emoji} Günün en kötü saati"
                    )
                }
            }
            }
        }
    }
}

@Composable
fun SporKarti(spor: SporUygunlugu) {
    val puanRengi = when {
        spor.puan >= 8 -> Color(0xFF69F0AE)
        spor.puan >= 5 -> Color(0xFFFFD54F)
        else -> Color(0xFFFF8A80)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .camKart()
            .padding(16.dp)
    ) {
        Text(
            text = "🏃 Spor uygunluğu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${spor.puan}",
                fontSize = 40.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Bold,
                color = puanRengi
            )
            Text(
                text = "/10",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 2.dp, top = 12.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = spor.baslik,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { spor.puan / 10f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = puanRengi,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = spor.aciklama, style = MaterialTheme.typography.bodyMedium)
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
