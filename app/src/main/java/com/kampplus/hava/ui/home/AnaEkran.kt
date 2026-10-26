package com.kampplus.hava.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import com.kampplus.hava.ui.theme.camKart
import kotlin.math.abs

const val KONUMUM_ANAHTARI = "konumum"

fun sayfaAnahtari(konum: KayitliKonum): String = "konum_${konum.id}"

@Composable
fun AnaEkran(
    onKonumlarimClick: () -> Unit,
    onGunSecildi: (anahtar: String, tarih: String) -> Unit,
    modifier: Modifier = Modifier,
    onKonumEkleClick: () -> Unit = {},
    viewModel: AnaEkranViewModel = viewModel()
) {
    val konumlar by viewModel.konumlar.collectAsStateWithLifecycle()
    val sayfaDurumu = rememberPagerState(pageCount = { konumlar.size + 1 })
    val gidilecekSayfa by viewModel.gidilecekSayfa.collectAsStateWithLifecycle()
    val kapsam = rememberCoroutineScope()

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Bu sayfa ortadaki sayfadan ne kadar uzakta? (0 = tam ortada)
                        val ofset = (sayfaDurumu.currentPage - sayfa) +
                            sayfaDurumu.currentPageOffsetFraction
                        val mesafe = abs(ofset).coerceIn(0f, 1f)

                        // 3B küp geçişi: sayfalar kenarlarından menteşeli gibi döner
                        cameraDistance = 16f * density
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (ofset > 0f) 1f else 0f,
                            pivotFractionY = 0.5f
                        )
                        rotationY = ofset * -35f
                        val olcek = 1f - 0.08f * mesafe
                        scaleX = olcek
                        scaleY = olcek
                        alpha = 1f - 0.3f * mesafe
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

        // Üstte: hangi sayfada olduğumuzu gösteren noktalar
        SayfaNoktalari(
            toplam = konumlar.size + 1,
            secili = sayfaDurumu.currentPage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        )

        // Altta: yüzen cam menü çubuğu
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .camKart(koseYaricap = 32.dp, opaklik = 0.22f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AltMenuDugmesi(
                simge = "📍",
                yazi = "Konumum",
                onClick = { kapsam.launch { sayfaDurumu.animateScrollToPage(0) } }
            )
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(elevation = 10.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)))
                    )
                    .clickable(onClick = onKonumEkleClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }
            AltMenuDugmesi(
                simge = "☰",
                yazi = "Konumlarım",
                onClick = onKonumlarimClick
            )
        }
    }
}

@Composable
private fun AltMenuDugmesi(simge: String, yazi: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = simge, fontSize = 20.sp, lineHeight = 24.sp, color = Color.White)
        Text(
            text = yazi,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
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

@Composable
fun SayfaNoktalari(toplam: Int, secili: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(toplam) { sira ->
            val secilmis = sira == secili
            val genislik by animateDpAsState(
                targetValue = if (secilmis) 22.dp else 8.dp,
                label = "nokta"
            )
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .height(8.dp)
                    .width(genislik)
                    .clip(RoundedCornerShape(50))
                    .background(if (secilmis) Color.White else Color.White.copy(alpha = 0.45f))
            )
        }
    }
}
