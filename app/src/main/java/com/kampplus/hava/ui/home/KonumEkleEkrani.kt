package com.kampplus.hava.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.Plakalar
import com.kampplus.hava.model.bayrakEmoji
import com.kampplus.hava.ui.LoadState

@Composable
fun KonumEkleEkrani(
    onGeriClick: () -> Unit,
    onEklendi: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KonumEkleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.eklendi) {
        if (uiState.eklendi) onEklendi()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TextButton(onClick = onGeriClick) {
            Text(text = "← Geri")
        }
        Text(text = "Konum ekle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.metin,
            onValueChange = { yeniMetin -> viewModel.metinDegisti(yeniMetin) },
            label = { Text(text = "İl, ilçe ya da şehir adı") },
            singleLine = true,
            trailingIcon = {
                if (uiState.metin.isNotEmpty()) {
                    IconButton(onClick = { viewModel.metinDegisti("") }) {
                        Text(text = "✕")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.ara() }),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.ekleniyor) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        val hata = uiState.hata
        if (hata != null) {
            Text(
                text = hata,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (uiState.ilOnerileri.isNotEmpty()) {
                item {
                    Text(
                        text = "🇹🇷 Türkiye'deki iller",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(uiState.ilOnerileri) { il ->
                    KonumSatiri(
                        ad = il,
                        altBilgi = "Türkiye · Plaka ${Plakalar.bul(il) ?: "-"}",
                        onClick = { viewModel.ilSecildi(il) },
                        onSilClick = null
                    )
                }
            }

            if (uiState.metin.trim().length >= 2) {
                item {
                    Button(
                        onClick = { viewModel.ara() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🔍 \"${uiState.metin.trim()}\" için tüm yerlerde ara")
                    }
                }
            }

            when (val sonuc = uiState.aramaSonucu) {
                null -> Unit

                is LoadState.Loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Empty -> item {
                    Text(text = "Sonuç bulunamadı. Yerin tam adını yazmayı dene.")
                }

                is LoadState.Error -> item {
                    Text(text = sonuc.message, color = MaterialTheme.colorScheme.error)
                }

                is LoadState.Success -> {
                    item {
                        Text(
                            text = "Arama sonuçları",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    items(sonuc.data, key = { city -> city.id }) { city ->
                        val ulkeAdi = if (city.countryCode == "TR") "Türkiye" else city.country
                        val konumBilgisi = listOfNotNull(city.admin1, ulkeAdi)
                            .distinct()
                            .joinToString(", ")
                        val mesafe = uiState.mesafeler[city.id]
                        val mesafeYazisi = if (mesafe != null) " · $mesafe km" else ""

                        KonumSatiri(
                            ad = city.name,
                            altBilgi = "${bayrakEmoji(city.countryCode)} $konumBilgisi$mesafeYazisi",
                            onClick = { viewModel.sehirSecildi(city) },
                            onSilClick = null
                        )
                    }
                }
            }
        }
    }
}