package com.kampplus.hava.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.KayitliKonum
import com.kampplus.hava.ui.forecast.ForecastScreen
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.theme.CamDugme
import kotlinx.coroutines.launch
import kotlin.math.abs

const val KONUMUM_ANAHTARI = "konumum"

fun sayfaAnahtari(konum: KayitliKonum): String = "konum_${konum.id}"

@Composable
fun AnaEkran(
    onKonumlarimClick: () -> Unit,
    onGunSecildi: (anahtar: String, tarih: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnaEkranViewModel = viewModel()
) {
    val konumlar by viewModel.konumlar.collectAsStateWithLifecycle()
    val sayfaDurumu = rememberPagerState(pageCount = { konumlar.size + 1 })
    val gidilecekSayfa by viewModel.gidilecekSayfa.collectAsStateWithLifecycle()
    val kapsam = rememberCoroutineScope()
    val etiketListesi = rememberLazyListState()

    LaunchedEffect(gidilecekSayfa, konumlar.size) {
        val hedef = gidilecekSayfa ?: return@LaunchedEffect
        if (hedef < sayfaDurumu.pageCount) {
            sayfaDurumu.animateScrollToPage(hedef)
            viewModel.sayfayaGidildi()
        }
    }

    // Sayfa değişince üstteki konum etiketleri de o konuma kaysın
    LaunchedEffect(sayfaDurumu.currentPage) {
        val hedef = sayfaDurumu.currentPage.coerceAtMost(konumlar.size)
        etiketListesi.animateScrollToItem(hedef)
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = sayfaDurumu,
            modifier = Modifier.fillMaxSize(),
            key = { sayfa ->
                if (sayfa == 0) {
                    KONUMUM_ANAHTARI
                } else {
                    konumlar.getOrNull(sayfa - 1)?.let { konum -> sayfaAnahtari(konum) } ?: "bos_$sayfa"
                }
            }
        ) { sayfa ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Kaydırırken sayfalar bir küpün yüzleri gibi dönsün (3B geçiş)
                        val sayfaFarki = (sayfaDurumu.currentPage - sayfa) +
                            sayfaDurumu.currentPageOffsetFraction
                        val mutlak = abs(sayfaFarki).coerceIn(0f, 1f)
                        cameraDistance = 16f * density
                        transformOrigin = TransformOrigin(if (sayfaFarki > 0f) 1f else 0f, 0.5f)
                        rotationY = sayfaFarki.coerceIn(-1f, 1f) * -40f
                        val olcek = 1f - 0.08f * mutlak
                        scaleX = olcek
                        scaleY = olcek
                        alpha = 1f - 0.3f * mutlak
                    }
            ) {
                if (sayfa == 0) {
                    val konumumViewModel: ForecastViewModel = viewModel(key = KONUMUM_ANAHTARI)
                    ForecastScreen(
                        onGeriClick = {},
                        ustCubukGoster = false,
                        onGunSecildi = { tarih -> onGunSecildi(KONUMUM_ANAHTARI, tarih) },
                        viewModel = konumumViewModel
                    )
                } else {
                    val konum = konumlar.getOrNull(sayfa - 1)
                    if (konum != null) {
                        val anahtar = sayfaAnahtari(konum)
                        ForecastScreen(
                            onGeriClick = {},
                            ustCubukGoster = false,
                            onGunSecildi = { tarih -> onGunSecildi(anahtar, tarih) },
                            viewModel = kayitliKonumViewModel(konum, anahtar)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                state = etiketListesi,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = KONUMUM_ANAHTARI) {
                    KonumEtiketi(
                        yazi = "📍 Konumum",
                        secili = sayfaDurumu.currentPage == 0,
                        onClick = { kapsam.launch { sayfaDurumu.animateScrollToPage(0) } }
                    )
                }
                itemsIndexed(konumlar, key = { _, konum -> konum.id }) { sira, konum ->
                    KonumEtiketi(
                        yazi = konum.ad,
                        secili = sayfaDurumu.currentPage == sira + 1,
                        onClick = { kapsam.launch { sayfaDurumu.animateScrollToPage(sira + 1) } }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            CamDugme(yazi = "☰", onClick = onKonumlarimClick)
        }
    }
}

@Composable
private fun KonumEtiketi(yazi: String, secili: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (secili) Color.White else Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = yazi,
            color = if (secili) Color(0xFF1A237E) else Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun kayitliKonumViewModel(konum: KayitliKonum, anahtar: String): ForecastViewModel {
    val sahip = checkNotNull(LocalViewModelStoreOwner.current)
    val varsayilan = (sahip as? HasDefaultViewModelProviderFactory)
        ?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty

    val ekstralar = MutableCreationExtras(varsayilan).apply {
        set(
            DEFAULT_ARGS_KEY,
            bundleOf(
                "lat" to konum.latitude.toString(),
                "lon" to konum.longitude.toString(),
                "name" to konum.ad
            )
        )
    }

    return viewModel(key = anahtar, extras = ekstralar)
}
