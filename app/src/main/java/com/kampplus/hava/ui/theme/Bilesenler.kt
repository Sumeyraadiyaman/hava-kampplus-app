package com.kampplus.hava.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.model.GiyimUyarisi
import com.kampplus.hava.model.UyariTuru
import kotlinx.coroutines.delay

/** Yarı saydam "buzlu cam" kart görünümü. */
fun Modifier.camKart(koseYaricap: Dp = 24.dp, opaklik: Float = 0.16f): Modifier {
    val sekil = RoundedCornerShape(koseYaricap)
    return this
        .clip(sekil)
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = opaklik + 0.06f),
                    Color.White.copy(alpha = opaklik)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f))
            ),
            shape = sekil
        )
}

@Composable
fun CamDugme(
    metin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vurgulu: Boolean = false
) {
    val sekil = RoundedCornerShape(50)
    val arkaPlan = if (vurgulu) {
        Brush.horizontalGradient(listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)))
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.16f))
        )
    }

    Box(
        modifier = modifier
            .shadow(elevation = if (vurgulu) 8.dp else 0.dp, shape = sekil)
            .clip(sekil)
            .background(arkaPlan)
            .border(1.dp, Color.White.copy(alpha = 0.45f), sekil)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = metin,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** "7 gün / 15 gün" seçici. Seçili olan beyaz dolguyla yumuşakça belirir. */
@Composable
fun GunSecici(secili: Int, onSecildi: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .camKart(koseYaricap = 50.dp, opaklik = 0.12f)
            .padding(4.dp)
    ) {
        listOf(7, 15).forEach { gun ->
            val secilmis = gun == secili
            val doluluk by animateFloatAsState(
                targetValue = if (secilmis) 1f else 0f,
                label = "gunSecici"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.9f * doluluk))
                    .clickable { onSecildi(gun) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$gun gün",
                    color = if (secilmis) Color(0xFF1E3A5F) else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun uyariRenkleri(tur: UyariTuru): List<Color> = when (tur) {
    UyariTuru.FIRTINA -> listOf(Color(0xFF6A1B9A), Color(0xFF283593))
    UyariTuru.SEMSIYE -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    UyariTuru.KAR, UyariTuru.DON -> listOf(Color(0xFF0277BD), Color(0xFF29B6F6))
    UyariTuru.SOGUK -> listOf(Color(0xFF37474F), Color(0xFF607D8B))
    UyariTuru.SICAK -> listOf(Color(0xFFD32F2F), Color(0xFFFF7043))
    UyariTuru.GUNES_KREMI -> listOf(Color(0xFFEF6C00), Color(0xFFFFA726))
    UyariTuru.RUZGAR -> listOf(Color(0xFF00695C), Color(0xFF26A69A))
    UyariTuru.HIRKA -> listOf(Color(0xFF6D4C41), Color(0xFFA1887F))
    UyariTuru.GUZEL -> listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
}

/**
 * Göze çarpan giyim uyarısı. Ekran açılınca yukarıdan yaylanarak gelir,
 * emoji ise sürekli hafifçe büyüyüp küçülerek dikkat çeker.
 */
@Composable
fun UyariBanner(uyari: GiyimUyarisi, modifier: Modifier = Modifier) {
    val giris = remember { Animatable(0f) }
    LaunchedEffect(uyari.tur) {
        giris.snapTo(0f)
        delay(250)
        giris.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 200f)
        )
    }

    val sonsuz = rememberInfiniteTransition(label = "uyari")
    val nabiz by sonsuz.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "nabiz"
    )

    val sekil = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = giris.value.coerceIn(0f, 1f)
                translationY = (1f - giris.value) * -60f * density
                scaleX = 0.9f + 0.1f * giris.value
                scaleY = 0.9f + 0.1f * giris.value
            }
            .shadow(elevation = 6.dp, shape = sekil)
            .clip(sekil)
            .background(Brush.horizontalGradient(uyariRenkleri(uyari.tur)))
            .border(1.dp, Color.White.copy(alpha = 0.35f), sekil)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = uyari.emoji,
            fontSize = 40.sp,
            lineHeight = 48.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = nabiz
                scaleY = nabiz
            }
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = uyari.baslik,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = uyari.aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

/** Günün en düşük-en yüksek sıcaklığını, gösterilen tüm günlere göre renkli bir çubukla çizer. */
@Composable
fun SicaklikCubugu(
    enDusuk: Double,
    enYuksek: Double,
    genelEnDusuk: Double,
    genelEnYuksek: Double,
    modifier: Modifier = Modifier
) {
    val aralik = (genelEnYuksek - genelEnDusuk).takeIf { it > 0.0 } ?: 1.0
    val bas = ((enDusuk - genelEnDusuk) / aralik).toFloat().coerceIn(0f, 1f)
    val son = ((enYuksek - genelEnDusuk) / aralik).toFloat().coerceIn(0f, 1f)

    Canvas(modifier = modifier.height(6.dp)) {
        val r = size.height / 2f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            cornerRadius = CornerRadius(r, r)
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4FC3F7), Color(0xFFFFEB3B), Color(0xFFFF7043)),
                startX = 0f,
                endX = size.width
            ),
            topLeft = Offset(size.width * bas, 0f),
            size = Size(size.width * (son - bas).coerceAtLeast(0.04f), size.height),
            cornerRadius = CornerRadius(r, r)
        )
    }
}

@Composable
fun YukleniyorGorunumu(metin: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = metin,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun HataGorunumu(mesaj: String, dugmeMetni: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp)
            .camKart()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "😕", fontSize = 48.sp, lineHeight = 56.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mesaj,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        CamDugme(metin = dugmeMetni, onClick = onClick, vurgulu = true)
    }
}

/**
 * Giyim önerilerini sağa kaydırılan kartlar halinde gösterir.
 * Sağda bir sonraki kartın ucu görünür, kullanıcı kaydırabileceğini anlar.
 */
@Composable
fun OneriKaydirici(
    oneriler: List<GiyimUyarisi>,
    baslik: String,
    modifier: Modifier = Modifier
) {
    val sayfaDurumu = rememberPagerState(pageCount = { oneriler.size })

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (oneriler.size > 1) {
                Text(
                    text = "${sayfaDurumu.currentPage + 1}/${oneriler.size}  kaydır ›",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        HorizontalPager(
            state = sayfaDurumu,
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(end = if (oneriler.size > 1) 40.dp else 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
        ) { sayfa ->
            UyariBanner(uyari = oneriler[sayfa], modifier = Modifier.fillMaxSize())
        }

        if (oneriler.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(oneriler.size) { sira ->
                    val secili = sira == sayfaDurumu.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (secili) 18.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = if (secili) 0.95f else 0.4f))
                    )
                }
            }
        }
    }
}

/** Başlığına dokununca açılıp kapanan bölüm. */
@Composable
fun AcilirBolum(
    baslik: String,
    modifier: Modifier = Modifier,
    baslangictaAcik: Boolean = false,
    icerik: @Composable () -> Unit
) {
    var acik by rememberSaveable { mutableStateOf(baslangictaAcik) }
    val okDonusu by animateFloatAsState(
        targetValue = if (acik) 180f else 0f,
        label = "acilirOk"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .camKart(koseYaricap = 20.dp)
                .clickable { acik = !acik }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (acik) "Gizle" else "Göster",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "▾",
                fontSize = 20.sp,
                lineHeight = 24.sp,
                modifier = Modifier.graphicsLayer { rotationZ = okDonusu }
            )
        }

        if (acik) {
            val giris = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                giris.animateTo(targetValue = 1f, animationSpec = tween(350))
            }
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .graphicsLayer {
                        alpha = giris.value
                        translationY = (1f - giris.value) * -20f * density
                    }
            ) {
                icerik()
            }
        }
    }
}
