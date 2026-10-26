package com.kampplus.hava.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.KayitliKonum
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.ParlakDugme
import com.kampplus.hava.ui.theme.menuGorunumu

@Composable
fun KonumlarimEkrani(
    onGeriClick: () -> Unit,
    onKonumSecildi: (sayfa: Int) -> Unit,
    onKonumEkleClick: () -> Unit,
    viewModel: AnaEkranViewModel,
    modifier: Modifier = Modifier
) {
    val konumlar by viewModel.konumlar.collectAsStateWithLifecycle()
    var silinecekKonum by remember { mutableStateOf<KayitliKonum?>(null) }

    HavaArkaPlani(gorunum = menuGorunumu, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            CamDugme(
                yazi = "← Geri",
                onClick = onGeriClick,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Konumlarım",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ana ekranda sağa sola kaydırarak ya da üstteki adlara dokunarak konumlar arasında geçebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    KonumSatiri(
                        ad = "📍 Konumum",
                        altBilgi = "Telefonunun bulunduğu yer, her zaman ilk sayfada",
                        onClick = { onKonumSecildi(0) },
                        onSilClick = null
                    )
                }
                itemsIndexed(konumlar, key = { _, konum -> konum.id }) { sira, konum ->
                    KonumSatiri(
                        ad = konum.ad,
                        altBilgi = konum.altBilgi,
                        onClick = { onKonumSecildi(sira + 1) },
                        onSilClick = { silinecekKonum = konum }
                    )
                }
            }

            ParlakDugme(
                yazi = "+ Konum ekle",
                onClick = onKonumEkleClick,
                vurgu = menuGorunumu.vurgu,
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
    onSilClick: (() -> Unit)?
) {
    val sekil = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), sekil)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ad,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = altBilgi,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        if (onSilClick != null) {
            IconButton(onClick = onSilClick) {
                Text(text = "🗑️")
            }
        } else {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}
