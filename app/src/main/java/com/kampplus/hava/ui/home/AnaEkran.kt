package com.kampplus.hava.ui.home

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    LaunchedEffect(gidilecekSayfa, konumlar.size) {
        val hedef = gidilecekSayfa ?: return@LaunchedEffect
        if (hedef < sayfaDurumu.pageCount) {
            sayfaDurumu.animateScrollToPage(hedef)
            viewModel.sayfayaGidildi()
        }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp)
        ) {
            SayfaNoktalari(
                toplam = konumlar.size + 1,
                secili = sayfaDurumu.currentPage,
                modifier = Modifier.align(Alignment.Center)
            )
            TextButton(
                onClick = onKonumlarimClick,
                modifier = Modifier.align(Alignment.CenterEnd),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(text = "☰ Konumlarım")
            }
        }
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

@Composable
fun SayfaNoktalari(toplam: Int, secili: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(toplam) { sira ->
            val renk = if (sira == secili) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.4f)
            }
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(renk)
            )
        }
    }
}