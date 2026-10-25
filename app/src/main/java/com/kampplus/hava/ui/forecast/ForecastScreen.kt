package com.kampplus.hava.ui.forecast

import com.kampplus.hava.model.gunlukNotlar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(
    onGeriClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onGeriClick) {
            Text(text = "← Geri")
        }
        Text(
            text = uiState.sehirAdi,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                Button(onClick = { viewModel.tekrarDene() }) {
                    Text(text = "Tekrar dene")
                }
            }

            is LoadState.Success -> {
                TahminIcerigi(
                    forecast = sonuc.data,
                    gunSayisi = uiState.gunSayisi,
                    onGunSayisiSecildi = { gun -> viewModel.gunSayisiSecildi(gun) }
                )
            }
        }
    }
}

@Composable
fun TahminIcerigi(
    forecast: Forecast,
    gunSayisi: Int,
    onGunSayisiSecildi: (Int) -> Unit
) {
    val anlik = forecast.current
    val durum = WeatherCondition.fromWeatherCode(anlik.weatherCode)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column {
                Text(
                    text = "${durum.emoji} ${anlik.temperature.roundToInt()}°",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = durum.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Hissedilen ${anlik.apparentTemperature.roundToInt()}° · " +
                            "Nem %${anlik.humidity} · " +
                            "Rüzgâr ${anlik.windSpeed.roundToInt()} km/s",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
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

        items(forecast.daily.take(gunSayisi), key = { gun -> gun.date }) { gun ->
            GunKarti(gun = gun)
        }
    }
}

@Composable
fun GunKarti(gun: DailyWeather) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)
    val notlar = gunlukNotlar(gun)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = durum.emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarihYaz(gun.date),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${durum.label} · Yağış %${gun.precipitationProbability}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${gun.tempMax.roundToInt()}° / ${gun.tempMin.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        notlar.forEach { not ->
            Text(
                text = not,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun tarihYaz(tarih: String): String {
    return try {
        val okuyucu = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val yazici = SimpleDateFormat("EEEE, d MMMM", Locale.forLanguageTag("tr-TR"))
        val date = okuyucu.parse(tarih) ?: return tarih
        yazici.format(date)
    } catch (e: Exception) {
        tarih
    }
}