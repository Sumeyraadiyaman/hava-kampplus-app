package com.kampplus.hava.ui.forecast

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HavaKalitesiSeviyesi
import com.kampplus.hava.model.HourlyWeather
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.tumOneriler
import com.kampplus.hava.model.gunlukNotlar
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.AcilirBolum
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.GunSecici
import com.kampplus.hava.ui.theme.HataGorunumu
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.HavaIkonu
import com.kampplus.hava.ui.theme.OneriKaydirici
import com.kampplus.hava.ui.theme.SicaklikCubugu
import com.kampplus.hava.ui.theme.YukleniyorGorunumu
import com.kampplus.hava.ui.theme.ayBul
import com.kampplus.hava.ui.theme.camKart
import com.kampplus.hava.ui.theme.havaGorunumu
import com.kampplus.hava.ui.theme.varsayilanGorunum
import kotlin.math.roundToInt

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
    ustCubukGoster: Boolean = true,
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

    val gorunum = when (val sonuc = uiState.sonuc) {
        is LoadState.Success -> {
            val anlik = sonuc.data.current
            havaGorunumu(
                kosul = WeatherCondition.fromWeatherCode(anlik.weatherCode),
                gunduz = anlik.isDay,
                ay = ayBul(anlik.time),
                sicaklik = anlik.temperature
            )
        }
        else -> varsayilanGorunum
    }

    HavaArkaPlani(gorunum = gorunum, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = if (ustCubukGoster) 0.dp else 36.dp)
        ) {
            if (ustCubukGoster) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.konumModu) {
                        CamDugme(metin = "← Geri", onClick = onGeriClick)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (uiState.konumModu) {
                        CamDugme(metin = "🔍 Şehir ara", onClick = onAramaClick)
                    }
                }
            }

            when (val sonuc = uiState.sonuc) {
                is LoadState.Loading -> YukleniyorGorunumu(
                    metin = if (uiState.konumModu) "Konumun bulunuyor…" else "Hava durumu yükleniyor…"
                )

                is LoadState.Empty -> HataGorunumu(
                    mesaj = "Tahmin verisi bulunamadı.",
                    dugmeMetni = "Tekrar dene",
                    onClick = { viewModel.tekrarDene() }
                )

                is LoadState.Error -> HataGorunumu(
                    mesaj = sonuc.message,
                    dugmeMetni = if (uiState.izinGerekli) "📍 Konum izni ver" else "Tekrar dene",
                    onClick = {
                        if (uiState.izinGerekli) {
                            izinIsteyici.launch(konumIzinleri)
                        } else {
                            viewModel.tekrarDene()
                        }
                    }
                )

                is LoadState.Success -> TahminIcerigi(
                    sehirAdi = uiState.sehirAdi,
                    forecast = sonuc.data,
                    gunSayisi = uiState.gunSayisi,
                    onGunSayisiSecildi = { gun -> viewModel.gunSayisiSecildi(gun) },
                    onGunSecildi = onGunSecildi,
                    altBosluk = if (ustCubukGoster) 32.dp else 120.dp
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
    onGunSecildi: (String) -> Unit,
    altBosluk: Dp = 32.dp
) {
    val bugun = forecast.daily.firstOrNull()
    val anlikSaat = forecast.current.time.take(13)
    val baslangic = forecast.hourly
        .indexOfFirst { saat -> saat.time.take(13) == anlikSaat }
        .coerceAtLeast(0)
    val sonraki24Saat = forecast.hourly.drop(baslangic).take(24)

    val gosterilenGunler = forecast.daily.take(gunSayisi)
    val genelEnDusuk = gosterilenGunler.minOfOrNull { gun -> gun.tempMin } ?: 0.0
    val genelEnYuksek = gosterilenGunler.maxOfOrNull { gun -> gun.tempMax } ?: 0.0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = altBosluk)
    ) {
        item {
            AnlikBolum(sehirAdi = sehirAdi, anlik = forecast.current, bugun = bugun)
        }

        if (bugun != null) {
            item {
                OneriKaydirici(
                    oneriler = tumOneriler(bugun),
                    baslik = "👕 Bugün ne giymelisin?"
                )
            }
        }

        item {
            SaatlikSerit(saatler = sonraki24Saat)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 Günlük tahmin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                GunSecici(secili = gunSayisi, onSecildi = onGunSayisiSecildi)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .camKart()
                    .padding(vertical = 4.dp)
            ) {
                gosterilenGunler.forEachIndexed { sira, gun ->
                    GunSatiri(
                        gun = gun,
                        etiket = gunEtiketi(sira, gun.date),
                        genelEnDusuk = genelEnDusuk,
                        genelEnYuksek = genelEnYuksek,
                        onClick = { onGunSecildi(gun.date) }
                    )
                    if (sira < gosterilenGunler.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                    }
                }
            }
        }

        item {
            AcilirBolum(baslik = "📊 Detaylar") {
                BilgiKutulari(
                    anlik = forecast.current,
                    bugun = bugun,
                    havaKalitesi = forecast.airQuality?.current
                )
            }
        }
    }
}

@Composable
fun AnlikBolum(sehirAdi: String, anlik: CurrentWeather, bugun: DailyWeather?) {
    val durum = WeatherCondition.fromWeatherCode(anlik.weatherCode)

    val sonsuz = rememberInfiniteTransition(label = "anlik")
    val suzulme by sonsuz.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "suzulme"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📍 $sehirAdi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        HavaIkonu(
            kosul = durum,
            gunduz = anlik.isDay,
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    translationY = (suzulme - 0.5f) * 12f * density
                    rotationZ = (suzulme - 0.5f) * 4f
                }
        )
        Text(
            text = "${anlik.temperature.roundToInt()}°",
            fontSize = 92.sp,
            lineHeight = 100.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.displayLarge.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.25f),
                    offset = Offset(0f, 6f),
                    blurRadius = 16f
                )
            )
        )
        Text(
            text = durum.label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        val hissedilen = "Hissedilen ${anlik.apparentTemperature.roundToInt()}°"
        Text(
            text = if (bugun != null) {
                "↑ ${bugun.tempMax.roundToInt()}°   ↓ ${bugun.tempMin.roundToInt()}°   ·   $hissedilen"
            } else {
                hissedilen
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun BugunNotKarti(notlar: List<String>, baslik: String = "💡 Bugün için") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .camKart()
            .padding(16.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        notlar.forEach { not ->
            Text(
                text = not,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
fun SaatlikSerit(
    saatler: List<HourlyWeather>,
    baslik: String = "🕐 Saatlik tahmin",
    ilkSaatSimdi: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .camKart()
            .padding(vertical = 14.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            itemsIndexed(saatler) { sira, saat ->
                val durum = WeatherCondition.fromWeatherCode(saat.weatherCode)
                val simdi = sira == 0 && ilkSaatSimdi

                Column(
                    modifier = Modifier
                        .width(62.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = if (simdi) 0.32f else 0.08f))
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (simdi) "Şimdi" else saatKismi(saat.time),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (simdi) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HavaIkonu(
                        kosul = durum,
                        gunduz = gunduzSaati(saat.time),
                        animasyonlu = false,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${saat.temperature.roundToInt()}°",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (saat.precipitationProbability > 0) "💧%${saat.precipitationProbability}" else " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB3E5FC)
                    )
                }
            }
        }
    }
}

@Composable
fun GunSatiri(
    gun: DailyWeather,
    etiket: String,
    genelEnDusuk: Double,
    genelEnYuksek: Double,
    onClick: () -> Unit
) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(84.dp)
        )
        HavaIkonu(kosul = durum, animasyonlu = false, modifier = Modifier.size(30.dp))
        Text(
            text = if (gun.precipitationProbability >= 20) "%${gun.precipitationProbability}" else "",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFB3E5FC),
            modifier = Modifier
                .width(38.dp)
                .padding(start = 4.dp)
        )
        Text(
            text = "${gun.tempMin.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.End,
            modifier = Modifier.width(34.dp)
        )
        SicaklikCubugu(
            enDusuk = gun.tempMin,
            enYuksek = gun.tempMax,
            genelEnDusuk = genelEnDusuk,
            genelEnYuksek = genelEnYuksek,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        Text(
            text = "${gun.tempMax.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(34.dp)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

private fun nemYorumu(nem: Int): String = when {
    nem < 30 -> "Kuru"
    nem < 60 -> "Rahat"
    nem < 80 -> "Nemli"
    else -> "Çok nemli"
}

private fun hissedilenYorumu(gercek: Double, hissedilen: Double): String = when {
    hissedilen < gercek - 2 -> "Daha soğuk hissettiriyor"
    hissedilen > gercek + 2 -> "Daha sıcak hissettiriyor"
    else -> "Gerçeğe yakın"
}

@Composable
fun BilgiKutulari(anlik: CurrentWeather, bugun: DailyWeather?, havaKalitesi: Int? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BilgiKutusu(
                baslik = "💧 Nem",
                deger = "%${anlik.humidity}",
                modifier = Modifier.weight(1f),
                alt = nemYorumu(anlik.humidity)
            )
            BilgiKutusu(
                baslik = "💨 Rüzgâr",
                deger = "${anlik.windSpeed.roundToInt()} km/sa",
                modifier = Modifier.weight(1f),
                alt = ruzgarYonu(anlik.windDirection)
            )
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BilgiKutusu(
                baslik = "🌡️ Basınç",
                deger = "${anlik.pressure.roundToInt()}",
                modifier = Modifier.weight(1f),
                alt = "hPa"
            )
            BilgiKutusu(
                baslik = "🤔 Hissedilen",
                deger = "${anlik.apparentTemperature.roundToInt()}°",
                modifier = Modifier.weight(1f),
                alt = hissedilenYorumu(anlik.temperature, anlik.apparentTemperature)
            )
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BilgiKutusu(
                baslik = "☀️ UV",
                deger = bugun?.uvIndexMax?.roundToInt()?.toString() ?: "-",
                modifier = Modifier.weight(1f),
                alt = bugun?.let { gun -> uvSeviyesi(gun.uvIndexMax) }
            )
            BilgiKutusu(
                baslik = "🌅 Gün doğumu",
                deger = bugun?.let { gun -> saatKismi(gun.sunrise) } ?: "-",
                modifier = Modifier.weight(1f),
                alt = bugun?.let { gun -> "🌇 Batış ${saatKismi(gun.sunset)}" }
            )
        }

        if (havaKalitesi != null) {
            val seviye = HavaKalitesiSeviyesi.fromAqi(havaKalitesi)
            BilgiKutusu(
                baslik = "🌫️ Hava kalitesi",
                deger = "$havaKalitesi · ${seviye.etiket}",
                modifier = Modifier.fillMaxWidth(),
                alt = "${seviye.emoji} Avrupa hava kalitesi indeksi"
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
            .camKart(koseYaricap = 20.dp)
            .padding(14.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = deger,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (alt != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = alt,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

private fun gunduzSaati(tarihSaat: String): Boolean =
    (tarihSaat.substringAfter('T').take(2).toIntOrNull() ?: 12) in 7..18
