package com.kampplus.hava.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.Plakalar
import com.kampplus.hava.model.bayrakEmoji
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.theme.CamDugme
import com.kampplus.hava.ui.theme.HavaArkaPlani
import com.kampplus.hava.ui.theme.menuGorunumu

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                CamDugme(metin = "← Geri", onClick = onGeriClick)
            }

            Text(
                text = "Konum ekle",
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "İl adını yazmaya başla. İlçe, köy ya da yurt dışından bir yer için tam adını yazıp ara.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.metin,
                onValueChange = { yeniMetin -> viewModel.metinDegisti(yeniMetin) },
                label = { Text(text = "İl, ilçe ya da şehir adı") },
                singleLine = true,
                trailingIcon = {
                    if (uiState.metin.isNotEmpty()) {
                        IconButton(onClick = { viewModel.metinDegisti("") }) {
                            Text(text = "✕", color = Color.White)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.ara() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.12f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                ),
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
                    color = Color(0xFFFFCDD2),
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
                            altBilgi = "🇹🇷 Türkiye · Plaka ${Plakalar.bul(il) ?: "-"}",
                            onClick = { viewModel.ilSecildi(il) },
                            onSilClick = null
                        )
                    }
                }

                if (uiState.metin.trim().length >= 2) {
                    item {
                        CamDugme(
                            metin = "🔍 \"${uiState.metin.trim()}\" için tüm yerlerde ara",
                            onClick = { viewModel.ara() },
                            vurgulu = true,
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
                        Text(text = "Sonuç bulunamadı. Yerin tam adını yazmayı dene.")
                    }

                    is LoadState.Error -> item {
                        Text(text = sonuc.message, color = Color(0xFFFFCDD2))
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
}
