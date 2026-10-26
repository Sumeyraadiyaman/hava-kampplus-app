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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.Plakalar
import com.kampplus.hava.model.bayrakEmoji
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.CamKart
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.ParlakDugme
import com.kampplus.hava.ui.theme.menuGorunumu

private val hataRengi = Color(0xFFFFCDD2)

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
                text = "Konum ekle",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "İl adını yazmaya başla ya da ilçe ve yurt dışı için tam adı yazıp ara.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.metin,
                onValueChange = { yeniMetin -> viewModel.metinDegisti(yeniMetin) },
                label = { Text(text = "İl, ilçe ya da şehir adı") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                trailingIcon = {
                    if (uiState.metin.isNotEmpty()) {
                        IconButton(onClick = { viewModel.metinDegisti("") }) {
                            Text(text = "✕")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White
                ),
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
                    CircularProgressIndicator(color = Color.White)
                }
            }

            val hata = uiState.hata
            if (hata != null) {
                Text(
                    text = hata,
                    color = hataRengi,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState.ilOnerileri.isNotEmpty()) {
                    item {
                        Text(
                            text = "🇹🇷 Türkiye'deki iller",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(uiState.ilOnerileri) { il ->
                        KonumSatiri(
                            ad = il,
                            altBilgi = "Türkiye, plaka ${Plakalar.bul(il) ?: "-"}",
                            onClick = { viewModel.ilSecildi(il) },
                            onSilClick = null
                        )
                    }
                }

                if (uiState.metin.trim().length >= 2) {
                    item {
                        ParlakDugme(
                            yazi = "🔍 \"${uiState.metin.trim()}\" için tüm yerlerde ara",
                            onClick = { viewModel.ara() },
                            vurgu = menuGorunumu.vurgu,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                when (val sonuc = uiState.aramaSonucu) {
                    null -> Unit

                    is LoadState.Loading -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    is LoadState.Empty -> item {
                        CamKart {
                            Text(text = "Sonuç bulunamadı. Yerin tam adını yazmayı dene.")
                        }
                    }

                    is LoadState.Error -> item {
                        CamKart {
                            Text(text = sonuc.message, color = hataRengi)
                        }
                    }

                    is LoadState.Success -> {
                        item {
                            Text(
                                text = "Arama sonuçları",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(sonuc.data, key = { city -> city.id }) { city ->
                            val ulkeAdi = if (city.countryCode == "TR") "Türkiye" else city.country
                            val konumBilgisi = listOfNotNull(city.admin1, ulkeAdi)
                                .distinct()
                                .joinToString(", ")
                            val mesafe = uiState.mesafeler[city.id]
                            val mesafeYazisi = if (mesafe != null) ", $mesafe km uzakta" else ""

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
}
