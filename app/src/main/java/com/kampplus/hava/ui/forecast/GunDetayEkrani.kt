package com.kampplus.hava.ui.forecast

import com.kampplus.hava.model.HavaKalitesiSeviyesi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.SporUygunlugu
import com.kampplus.hava.model.sporUygunlugu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.HourlyWeather
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.gunlukNotlar
import com.kampplus.hava.ui.LoadState
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TextButton(onClick = onGeriClick) {
            Text(text = "← Geri")
        }

        if (forecast == null || gun == null) {
            Text(text = "Bu gün için veri bulunamadı")
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = tarihYaz(gun.date), style = MaterialTheme.typography.titleLarge)
                Text(text = "📍 $sehirAdi", style = MaterialTheme.typography.bodyMedium)
                Text(text = durum.emoji, style = MaterialTheme.typography.displayMedium)
                Text(
                    text = "${gun.tempMax.roundToInt()}° / ${gun.tempMin.roundToInt()}°",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(text = durum.label, style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            BugunNotKarti(
                notlar = gunlukNotlar(gun),
                baslik = "👕 Ne giymeli, nelere dikkat etmeli?"
            )
        }

        if (saatler.isNotEmpty()) {
            item {
                SaatlikSerit(saatler = saatler, baslik = "Saat saat", ilkSaatSimdi = false)
            }
        }

        item {
            SporKarti(spor = sporUygunlugu(gun, saatler, havaKalitesi))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Detaylar", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BilgiKutusu(
                        baslik = "🌧️ Yağış ihtimali",
                        deger = "%${gun.precipitationProbability}",
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "☔ Yağış miktarı",
                        deger = ondalik(gun.precipitationSum),
                        alt = "mm",
                        modifier = Modifier.weight(1f)
                    )
                    BilgiKutusu(
                        baslik = "💨 En yüksek rüzgâr",
                        deger = "${gun.windSpeedMax.roundToInt()}",
                        alt = "km/sa",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    BilgiKutusu(
                        baslik = "☀️ UV",
                        deger = "${gun.uvIndexMax.roundToInt()}",
                        alt = uvSeviyesi(gun.uvIndexMax),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    BilgiKutusu(
                        baslik = "⏱️ Gün uzunluğu",
                        deger = gunUzunlugu(gun.sunrise, gun.sunset),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (havaKalitesi != null) {
                    val seviye = HavaKalitesiSeviyesi.fromAqi(havaKalitesi)
                    BilgiKutusu(
                        baslik = "🌫️ Hava kalitesi",
                        deger = "$havaKalitesi",
                        alt = "${seviye.emoji} ${seviye.etiket} · günün en kötü saati",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
@Composable
fun SporKarti(spor: SporUygunlugu) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F5E9))
            .padding(12.dp)
    ) {
        Text(text = "🏃 Spor uygunluğu", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${spor.puan}/10",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = spor.baslik, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { spor.puan / 10f },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = spor.aciklama, style = MaterialTheme.typography.bodyMedium)
        if (spor.enUygunSaat != null) {
            Text(
                text = "⏰ En uygun saat: ${spor.enUygunSaat}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}