package com.kampplus.hava.ui.home

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.model.KayitliKonum

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        TextButton(onClick = onGeriClick) {
            Text(text = "← Geri")
        }
        Text(text = "Konumlarım", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                KonumSatiri(
                    ad = "📍 Konumum",
                    altBilgi = "Telefonunun bulunduğu yer",
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

        Button(
            onClick = onKonumEkleClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "+ Konum ekle")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFE3F2FD))
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ad, style = MaterialTheme.typography.titleMedium)
            Text(text = altBilgi, style = MaterialTheme.typography.bodySmall)
        }
        if (onSilClick != null) {
            IconButton(onClick = onSilClick) {
                Text(text = "🗑️")
            }
        }
    }
}