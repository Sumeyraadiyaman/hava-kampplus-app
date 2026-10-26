package com.kampplus.hava.ui.forecast

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.kampplus.hava.model.gunlukNotlar
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.CamKart
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.KoyuYazi
import com.kampplus.hava.ui.theme.ParlakDugme
import com.kampplus.hava.ui.theme.havaGorunumu
import com.kampplus.hava.ui.theme.varsayilanGorunum
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private val konumIzinleri = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private val yagmurMavisi = Color(0xFFB3E5FC)

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

    val basari = uiState.sonuc as? LoadState.Success
    val gorunum = if (basari != null) {
        val anlik = basari.data.current
        havaGorunumu(
            kosul = WeatherCondition.fromWeatherCode(anlik.weatherCode),
            gunduz = anlik.isDay,
            sicaklik = anlik.temperature
        )
    } else {
        varsayilanGorunum
    }

    // Ekran açılınca bugünün en önemli notu üstten kayarak gelsin
    val bugunkuNotlar = basari?.data?.daily?.firstOrNull()?.let { gun -> gunlukNotlar(gun) } ?: emptyList()
    val oneCikanNot = bugunkuNotlar.firstOrNull()
    var uyariGoster by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.sehirAdi, oneCikanNot) {
        if (oneCikanNot != null) {
            delay(600)
            uyariGoster = true
            delay(5500)
            uyariGoster = false
        }
    }

    HavaArkaPlani(gorunum = gorunum, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = if (ustCubukGoster) 0.dp else 56.dp)
            ) {
                if (ustCubukGoster) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!uiState.konumModu) {
                            CamDugme(yazi = "← Geri", onClick = onGeriClick)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (uiState.konumModu) {
                            CamDugme(yazi = "🔍 Şehir ara", onClick = onAramaClick)
                        }
                    }
                }

                when (val sonuc = uiState.sonuc) {
                    is LoadState.Loading -> YukleniyorGorunumu(
                        yazi = uiState.sehirAdi.ifBlank { "Hava durumu yükleniyor…" }
                    )

                    is LoadState.Empty -> DurumKarti(
                        emoji = "🤷",
                        mesaj = "Bu konum için tahmin verisi bulunamadı.",
                        dugmeYazisi = "Tekrar dene",
                        vurgu = gorunum.vurgu,
                        onDugmeClick = { viewModel.tekrarDene() }
                    )

                    is LoadState.Error -> DurumKarti(
                        emoji = if (uiState.izinGerekli) "📍" else "📡",
                        mesaj = sonuc.message,
                        dugmeYazisi = if (uiState.izinGerekli) "Konum izni ver" else "Tekrar dene",
                        vurgu = gorunum.vurgu,
                        onDugmeClick = {
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
                        vurgu = gorunum.vurgu,
                        onGunSayisiSecildi = { gun -> viewModel.gunSayisiSecildi(gun) },
                        onGunSecildi = onGunSecildi
                    )
                }
            }

            AnimatedVisibility(
                visible = uyariGoster && oneCikanNot != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .systemBarsPadding()
                    .padding(top = if (ustCubukGoster) 64.dp else 60.dp),
                enter = slideInVertically { yukseklik -> -yukseklik } + fadeIn(),
                exit = slideOutVertically { yukseklik -> -yukseklik } + fadeOut()
            ) {
                OneCikanUyari(
                    not = oneCikanNot ?: "",
                    vurgu = gorunum.vurgu,
                    onClick = { uyariGoster = false }
                )
            }
        }
    }
}

@Composable
private fun YukleniyorGorunumu(yazi: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = yazi, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DurumKarti(
    emoji: String,
    mesaj: String,
    dugmeYazisi: String,
    vurgu: Color,
    onDugmeClick: () -> Unit
) {
    CamKart(modifier = Modifier.padding(top = 64.dp)) {
        Text(
            text = emoji,
            fontSize = 48.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mesaj,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        ParlakDugme(
            yazi = dugmeYazisi,
            onClick = onDugmeClick,
            vurgu = vurgu,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun OneCikanUyari(not: String, vurgu: Color, onClick: () -> Unit) {
    val emoji = not.substringBefore(" ")
    val metin = not.substringAfter(" ", not)
    val sekil = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = sekil)
            .clip(sekil)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 30.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bugün için hatırlatma",
                style = MaterialTheme.typography.labelMedium,
                color = vurgu,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = metin,
                style = MaterialTheme.typography.bodyLarge,
                color = KoyuYazi,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TahminIcerigi(
    sehirAdi: String,
    forecast: Forecast,
    gunSayisi: Int,
    vurgu: Color,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            AnlikBolum(sehirAdi = sehirAdi, anlik = forecast.current, bugun = bugun)
        }

        if (bugun != null) {
            item {
                GiyimKarti(
                    notlar = gunlukNotlar(bugun),
                    baslik = "👕 Bugün ne giymeli?",
                    vurgu = vurgu
                )
            }
        }

        item {
            SaatlikSerit(saatler = sonraki24Saat)
        }

        item {
            GunlukListe(
                gunler = forecast.daily.take(gunSayisi),
                gunSayisi = gunSayisi,
                vurgu = vurgu,
                onGunSayisiSecildi = onGunSayisiSecildi,
                onGunSecildi = onGunSecildi
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
    val gecis = rememberInfiniteTransition(label = "emoji")
    val yukseklik by gecis.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yukseklik"
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
        Text(
            text = durum.emoji,
            fontSize = 72.sp,
            modifier = Modifier.graphicsLayer { translationY = yukseklik.dp.toPx() }
        )
        Text(
            text = "${anlik.temperature.roundToInt()}°",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 96.sp,
                fontWeight = FontWeight.Light
            )
        )
        Text(text = durum.label, style = MaterialTheme.typography.titleLarge)
        if (bugun != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "↑ ${bugun.tempMax.roundToInt()}°   ↓ ${bugun.tempMin.roundToInt()}°   Hissedilen ${anlik.apparentTemperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/** Ne giyileceğini anlatan, göze çarpan beyaz kart. Kenarı hafifçe nabız gibi atar. */
@Composable
fun GiyimKarti(notlar: List<String>, baslik: String, vurgu: Color) {
    val gecis = rememberInfiniteTransition(label = "giyim")
    val parlama by gecis.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parlama"
    )
    val sekil = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = sekil)
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.95f))
            .border(2.dp, vurgu.copy(alpha = parlama), sekil)
            .padding(16.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = vurgu
        )
        Spacer(modifier = Modifier.height(8.dp))
        notlar.forEach { not ->
            val emoji = not.substringBefore(" ")
            val metin = not.substringAfter(" ", not)
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(vurgu.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = metin,
                    style = MaterialTheme.typography.bodyLarge,
                    color = KoyuYazi
                )
            }
        }
    }
}

@Composable
fun SaatlikSerit(
    saatler: List<HourlyWeather>,
    baslik: String = "🕐 Saatlik tahmin",
    ilkSaatSimdi: Boolean = true
) {
    CamKart {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(saatler) { sira, saat ->
                val durum = WeatherCondition.fromWeatherCode(saat.weatherCode)
                val simdi = sira == 0 && ilkSaatSimdi
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (simdi) Color.White.copy(alpha = 0.28f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (simdi) "Şimdi" else saatKismi(saat.time),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (simdi) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = durum.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${saat.temperature.roundToInt()}°",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (saat.precipitationProbability > 0) "%${saat.precipitationProbability}" else " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = yagmurMavisi
                    )
                }
            }
        }
    }
}

@Composable
fun GunlukListe(
    gunler: List<DailyWeather>,
    gunSayisi: Int,
    vurgu: Color,
    onGunSayisiSecildi: (Int) -> Unit,
    onGunSecildi: (String) -> Unit
) {
    if (gunler.isEmpty()) return
    val enDusuk = gunler.minOf { gun -> gun.tempMin }
    val enYuksek = gunler.maxOf { gun -> gun.tempMax }

    CamKart {
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
            GunSecici(gunSayisi = gunSayisi, onSecildi = onGunSayisiSecildi, vurgu = vurgu)
        }
        Spacer(modifier = Modifier.height(8.dp))
        gunler.forEachIndexed { sira, gun ->
            GunSatiri(
                gun = gun,
                etiket = gunEtiketi(sira, gun.date),
                enDusuk = enDusuk,
                enYuksek = enYuksek,
                onClick = { onGunSecildi(gun.date) }
            )
            if (sira < gunler.lastIndex) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            }
        }
    }
}

@Composable
fun GunSecici(gunSayisi: Int, onSecildi: (Int) -> Unit, vurgu: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(3.dp)
    ) {
        listOf(7, 15).forEach { gun ->
            val secili = gun == gunSayisi
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (secili) Color.White else Color.Transparent)
                    .clickable { onSecildi(gun) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$gun gün",
                    color = if (secili) vurgu else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GunSatiri(
    gun: DailyWeather,
    etiket: String,
    enDusuk: Double,
    enYuksek: Double,
    onClick: () -> Unit
) {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(78.dp)
        )
        Text(text = durum.emoji, fontSize = 22.sp)
        Text(
            text = if (gun.precipitationProbability >= 20) "%${gun.precipitationProbability}" else "",
            style = MaterialTheme.typography.labelMedium,
            color = yagmurMavisi,
            modifier = Modifier
                .width(40.dp)
                .padding(start = 6.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${gun.tempMin.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.End,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        SicaklikCubugu(
            enDusukGun = gun.tempMin,
            enYuksekGun = gun.tempMax,
            enDusuk = enDusuk,
            enYuksek = enYuksek,
            modifier = Modifier
                .width(56.dp)
                .height(6.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${gun.tempMax.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(32.dp)
        )
    }
}

/** O günün sıcaklık aralığını, listedeki tüm günlere göre renkli bir çubukla gösterir. */
@Composable
fun SicaklikCubugu(
    enDusukGun: Double,
    enYuksekGun: Double,
    enDusuk: Double,
    enYuksek: Double,
    modifier: Modifier = Modifier
) {
    val aralik = (enYuksek - enDusuk).coerceAtLeast(1.0)
    val basOran = ((enDusukGun - enDusuk) / aralik).toFloat().coerceIn(0f, 1f)
    val sonOran = ((enYuksekGun - enDusuk) / aralik).toFloat().coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val koseler = CornerRadius(size.height / 2f)
        drawRoundRect(color = Color.White.copy(alpha = 0.2f), cornerRadius = koseler)
        val bas = size.width * basOran
        val genislik = (size.width * (sonOran - basOran)).coerceAtLeast(size.height)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4FC3F7), Color(0xFFFFD54F), Color(0xFFFF7043)),
                startX = 0f,
                endX = size.width
            ),
            topLeft = Offset(bas, 0f),
            size = Size(genislik, size.height),
            cornerRadius = koseler
        )
    }
}

@Composable
fun BilgiKutulari(anlik: CurrentWeather, bugun: DailyWeather?, havaKalitesi: Int? = null) {
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
                baslik = "💧 Nem",
                deger = "%${anlik.humidity}",
                alt = nemYorumu(anlik.humidity),
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "💨 Rüzgâr",
                deger = "${anlik.windSpeed.roundToInt()} km/sa",
                alt = ruzgarYonu(anlik.windDirection),
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
                baslik = "🌡️ Basınç",
                deger = "${anlik.pressure.roundToInt()}",
                alt = "hPa",
                modifier = Modifier.weight(1f)
            )
            BilgiKutusu(
                baslik = "🤔 Hissedilen",
                deger = "${anlik.apparentTemperature.roundToInt()}°",
                alt = hissedilenYorumu(anlik),
                modifier = Modifier.weight(1f)
            )
        }

        if (bugun != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BilgiKutusu(
                    baslik = "☀️ UV indeksi",
                    deger = "${bugun.uvIndexMax.roundToInt()}",
                    alt = uvSeviyesi(bugun.uvIndexMax),
                    modifier = Modifier.weight(1f)
                )
                BilgiKutusu(
                    baslik = "🌅 Gün doğumu",
                    deger = saatKismi(bugun.sunrise),
                    alt = "🌇 Batış ${saatKismi(bugun.sunset)}",
                    modifier = Modifier.weight(1f)
                )
            }
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
    val sekil = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), sekil)
            .padding(14.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = deger,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (alt != null) {
            Text(
                text = alt,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

private fun nemYorumu(nem: Int): String = when {
    nem < 30 -> "Kuru hava"
    nem < 65 -> "Rahat"
    else -> "Nemli"
}

private fun hissedilenYorumu(anlik: CurrentWeather): String {
    val fark = anlik.apparentTemperature - anlik.temperature
    return when {
        abs(fark) < 2 -> "Gerçeğe yakın"
        fark < 0 -> "Daha soğuk hissettiriyor"
        else -> "Daha sıcak hissettiriyor"
    }
}
