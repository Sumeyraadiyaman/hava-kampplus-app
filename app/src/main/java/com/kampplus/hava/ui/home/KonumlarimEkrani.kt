package com.kampplus.hava.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.KayitliKonum
import com.kampplus.hava.model.KonumOzeti
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.HavaIkonu
import com.kampplus.hava.ui.theme.ayBul
import com.kampplus.hava.ui.theme.havaGorunumu
import com.kampplus.hava.ui.theme.camKart
import com.kampplus.hava.ui.theme.menuGorunumu
import kotlin.math.roundToInt

@Composable
fun KonumlarimEkrani(
    onGeriClick: () -> Unit,
    onKonumSecildi: (sayfa: Int) -> Unit,
    onKonumEkleClick: () -> Unit,
    viewModel: AnaEkranViewModel,
    modifier: Modifier = Modifier
) {
    val konumlar by viewModel.konumlar.collectAsStateWithLifecycle()
    val ozetler by viewModel.ozetler.collectAsStateWithLifecycle()
    var silinecekKonum by remember { mutableStateOf<KayitliKonum?>(null) }

    LaunchedEffect(Unit) {
        viewModel.konumumOzetiniYukle()
    }

    HavaArkaPlani(gorunum = menuGorunumu, modifier = modifier) {
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

            Text(
                text = "Konumlarım",
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sıralamak için ▲ ▼, silmek için 🗑️ kullan",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    KonumKarti(
                        ad = "📍 Konumum",
                        altBilgi = "Bulunduğun yer · Otomatik",
                        ozet = ozetler[KONUMUM_ANAHTARI],
                        onClick = { onKonumSecildi(0) },
                        onSilClick = null
                    )
                }

                itemsIndexed(konumlar, key = { _, konum -> konum.id }) { sira, konum ->
                    val yukariTasi: (() -> Unit)? = if (sira > 0) {
                        { viewModel.tasi(konum.id, -1) }
                    } else {
                        null
                    }
                    val asagiTasi: (() -> Unit)? = if (sira < konumlar.lastIndex) {
                        { viewModel.tasi(konum.id, 1) }
                    } else {
                        null
                    }

                    KonumKarti(
                        ad = konum.ad,
                        altBilgi = konum.altBilgi,
                        ozet = ozetler[konum.id],
                        onClick = { onKonumSecildi(sira + 1) },
                        onSilClick = { silinecekKonum = konum },
                        onYukariClick = yukariTasi,
                        onAsagiClick = asagiTasi
                    )
                }

                if (konumlar.isEmpty()) {
                    item {
                        Text(
                            text = "Henüz konum eklemedin. Aşağıdaki düğmeyle istediğin şehri ekleyebilirsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            CamDugme(
                metin = "＋  Konum ekle",
                onClick = onKonumEkleClick,
                vurgulu = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }

    val hedef = silinecekKonum
    if (hedef != null) {
        AlertDialog(
            onDismissRequest = { silinecekKonum = null },
            title = { Text(text = "Konumu sil") },
            text = { Text(text = "${hedef.ad} konumlarından silinsin mi?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.sil(hedef.id)
                    silinecekKonum = null
                }) {
                    Text(text = "Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { silinecekKonum = null }) {
                    Text(text = "Vazgeç")
                }
            }
        )
    }
}

@Composable
fun KonumSatiri(
    ad: String,
    altBilgi: String,
    onClick: () -> Unit,
    onSilClick: (() -> Unit)?,
    onYukariClick: (() -> Unit)? = null,
    onAsagiClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .camKart(koseYaricap = 20.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ad,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = altBilgi,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        if (onYukariClick != null || onAsagiClick != null) {
            KucukDugme(simge = "▲", onClick = onYukariClick)
            KucukDugme(simge = "▼", onClick = onAsagiClick)
        }
        if (onSilClick != null) {
            KucukDugme(simge = "🗑️", onClick = onSilClick)
        }
    }
}

@Composable
private fun KucukDugme(simge: String, onClick: (() -> Unit)?) {
    val aktif = onClick != null
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (aktif) 0.18f else 0.05f))
            .clickable(enabled = aktif) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = simge,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = if (aktif) 1f else 0.3f)
        )
    }
}

/**
 * İlham görselindeki gibi büyük konum kartı: kendi havasının renginde,
 * solda büyük sıcaklık ve konum adı, sağda 3B hava ikonu.
 */
@Composable
fun KonumKarti(
    ad: String,
    altBilgi: String,
    ozet: KonumOzeti?,
    onClick: () -> Unit,
    onSilClick: (() -> Unit)?,
    onYukariClick: (() -> Unit)? = null,
    onAsagiClick: (() -> Unit)? = null
) {
    val kosul = ozet?.let { WeatherCondition.fromWeatherCode(it.havaKodu) }
    val renkler = if (ozet != null && kosul != null) {
        havaGorunumu(
            kosul = kosul,
            gunduz = ozet.gunduz,
            ay = ayBul(ozet.zaman),
            sicaklik = ozet.sicaklik
        ).renkler
    } else {
        listOf(Color(0xFF4A3A9C), Color(0xFF8A6FD8))
    }
    val sekil = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = sekil)
            .clip(sekil)
            .background(Brush.linearGradient(renkler))
            .border(1.dp, Color.White.copy(alpha = 0.3f), sekil)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (ozet != null) "${ozet.sicaklik.roundToInt()}°" else "--°",
                fontSize = 46.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            if (ozet != null) {
                Text(
                    text = "↑ ${ozet.enYuksek.roundToInt()}°   ↓ ${ozet.enDusuk.roundToInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ad,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = altBilgi,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            if (kosul != null && ozet != null) {
                HavaIkonu(
                    kosul = kosul,
                    gunduz = ozet.gunduz,
                    modifier = Modifier.size(78.dp)
                )
                Text(
                    text = kosul.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            } else {
                Box(
                    modifier = Modifier.size(78.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                }
            }
            if (onSilClick != null || onYukariClick != null || onAsagiClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    if (onYukariClick != null || onAsagiClick != null) {
                        KucukDugme(simge = "▲", onClick = onYukariClick)
                        KucukDugme(simge = "▼", onClick = onAsagiClick)
                    }
                    if (onSilClick != null) {
                        KucukDugme(simge = "🗑️", onClick = onSilClick)
                    }
                }
            }
        }
    }
}
