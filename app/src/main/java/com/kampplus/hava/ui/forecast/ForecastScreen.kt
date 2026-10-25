package com.kampplus.hava.ui.forecast

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kampplus.hava.model.HavaKalitesiSeviyesi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyWeather
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.gunlukNotlar
import com.kampplus.hava.ui.LoadState
import kotlin.math.roundToInt

private val kartRengi = Color(0xFFE3F2FD)
private val notRengi = Color(0xFFFFF3E0)

private val konumIzinleri = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun ForecastScreen(
    onGeriClick: () -> Unit,
    modifier: Modifier = Modifier,
    onGunSecildi: (String) -> Unit = {},
    onAramaClick: () -> Unit = {},
    viewModel: ForecastViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val izinIsteyici = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { sonuclar ->
        viewModel.izinSonucu(verildi = sonuclar.values.any { verildi -> verildi })
    }

    var izinSoruldu by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.izinGerekli) {
        if (uiState.izinGerekli && !izinSoruldu) {
            izinSoruldu = true
            izinIsteyici.launch(konumIzinleri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!uiState.konumModu) {
                TextButton(onClick = onGeriClick) {
                    Text(text = "← Geri")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.konumModu) {
                TextButton(onClick = onAramaClick) {
                    Text(text = "🔍 Şehir ara")
                }
            }
        }

        when (val sonuc = uiState.sonuc) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Empty -> {
                Text(text = "Tahmin verisi bulunamadı")
            }

            is LoadState.Error -> {
                Text(
                    text = sonuc.message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.izinGerekli) {
                    Button(onClick = { izinIsteyici.launch(konumIzinleri) }) {
                        Text(text = "📍 Konum izni ver")
                    }
                } else {
                    Button(onClick = { viewModel.tekrarDene() }) {
                        Text(text = "Tekrar dene")
                    }
                }
            }

            is LoadState.Success -> {
                TahminIcerigi(
                    sehirAdi = uiState.sehirAdi,
                    forecast = sonuc.data,
                    gunSayisi = uiState.gunSayisi,
                    onGunSayisiSecildi = { gun -> viewModel.gunSayisiSecildi(gun) },
                    onGunSecildi = onGunSecildi
                )
            }
        }
    }
}

@Composable
fun TahminIcerigi(
    sehirAdi: String,
    forecast: Forecast,
    gunSayisi: Int,
    onGunSayisiSecildi: (Int) -> Unit,
    onGunSecildi: (String) -> Unit
) {
    val bugun = forecast.daily.firstOrNull()
    val anlikSaat = forecast.current.time.take(13)
    val baslangic = forecast.hourly
        .indexOfFirst { saat -> saat.time.take(13) == anlikSaat }
        .coerceAtLeast(0)
    val sonraki24Saat = forecast.hourly.drop(baslangic).take(24)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            AnlikBolum(sehirAdi = sehirAdi, anlik = forecast.current, bugun = bugun)
        }

        if (bugun != null) {
            item {
                BugunNotKarti(notlar = gunlukNotlar(bugun))
            }
        }

        item {
            SaatlikSerit(saatler = sonraki24Saat)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = gunSayisi == 7,
                    onClick = { onGunSayisiSecildi(7) },
                    label = { Text(text = "7 gün") }
                )
                FilterChip(
                    selected = gunSayisi == 15,
                    onClick = { onGunSayisiSecildi(15) },
                    label = { Text(text = "15 gün") }
                )
            }
        }

        itemsIndexed(
            forecast.daily.take(gunSayisi),
            key = { _, gun -> gun.date }
        ) { sira, gun ->
            GunSatiri(
                gun = gun,
                etiket = gunEtiketi(sira, gun.date),
                onClick = { onGunSecildi(gun.date) }
            )
        }

        item {
            BilgiKutulari(
                anlik = forecast.current,
                bugun = bugun,
                havaKalitesi = forecast.airQuality?.current
            )
        }
    }
}

@Composable
fun AnlikBolum(sehirAdi: String, anlik: CurrentWeather, bugun: DailyWeather?) {
    val durum = WeatherCondition.fromWeatherCode(anlik.weatherCode)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📍 $sehirAdi", style = MaterialTheme.typography.titleLarge)
        Text(text = durum.emoji, style = MaterialTheme.typography.displayMedium)
        Text(
            text = "${anlik.temperature.roundToInt()}°",
            style = MaterialTheme.typography.displayLarge
        )
        Text(text = durum.label, style = MaterialTheme.typography.titleMedium)
        if (bugun != null) {
            Text(
                text = "En yüksek ${bugun.tempMax.roundToInt()}° · En düşük ${bugun.tempMin.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BugunNotKarti(notlar: List<String>, baslik: String = "💡 Bugün için") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(notRengi)
            .padding(12.dp)
    ) {
        Text(text = baslik, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        notlar.forEach { not ->
            Text(text = not, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SaatlikSerit(
    saatler: List<HourlyWeather>,
    baslik: String = "Saatlik tahmin",
    ilkSaatSimdi: Boolean = true
) {
    Column {
        Text(text = baslik, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(saatler) { sira, saat ->
                val durum = WeatherCondition.fromWeatherCode(saat.weatherCode)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(kartRengi)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (sira == 0 && ilkSaatSimdi) "Şimdi" else saatKismi(saat.time),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(text = durum.emoji, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${saat.temperature.roundToInt()}°",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "💧%${saat.precipitationProbability}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun GunSatiri(gun: DailyWeather, etiket: String, onClick: () -> Unit) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(kartRengi)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(text = durum.emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "💧%${gun.precipitationProbability}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = "${gun.tempMax.roundToInt()}° / ${gun.tempMin.roundToInt()}°",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "›", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun BilgiKutulari(anlik: CurrentWeather, bugun: DailyWeather?, havaKalitesi: Int? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Detaylar", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BilgiKutusu(
                baslik = "💧 Nem",
                deger = "%${anlik.humidity}",
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "💨 Rüzgâr",
                deger = "${anlik.windSpeed.roundToInt()} km/sa",
                alt = ruzgarYonu(anlik.windDirection),
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "🌡️ Basınç",
                deger = "${anlik.pressure.roundToInt()}",
                alt = "hPa",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BilgiKutusu(
                baslik = "🤔 Hissedilen",
                deger = "${anlik.apparentTemperature.roundToInt()}°",
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "☀️ UV",
                deger = bugun?.uvIndexMax?.roundToInt()?.toString() ?: "-",
                alt = bugun?.let { gun -> uvSeviyesi(gun.uvIndexMax) },
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "🌅 Gün doğumu",
                deger = bugun?.let { gun -> saatKismi(gun.sunrise) } ?: "-",
                alt = bugun?.let { gun -> "🌇 Batış ${saatKismi(gun.sunset)}" },
                modifier = Modifier.weight(1f)
            )
        }

        if (havaKalitesi != null) {
            val seviye = HavaKalitesiSeviyesi.fromAqi(havaKalitesi)
            BilgiKutusu(
                baslik = "🌫️ Hava kalitesi",
                deger = "$havaKalitesi",
                alt = "${seviye.emoji} ${seviye.etiket}",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BilgiKutusu(
    baslik: String,
    deger: String,
    modifier: Modifier = Modifier,
    alt: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(kartRengi)
            .padding(12.dp)
    ) {
        Text(text = baslik, style = MaterialTheme.typography.labelMedium)
        Text(text = deger, style = MaterialTheme.typography.titleLarge)
        if (alt != null) {
            Text(text = alt, style = MaterialTheme.typography.labelSmall)
        }
    }
}