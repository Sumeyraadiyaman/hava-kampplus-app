package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.model.Sehir
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CityUiState(
    val aramaMetni: String = "",
    val sehirler: List<Sehir> = emptyList(),
    val seciliSehir: String? = null
)

class CityViewModel : ViewModel() {

    private val repository = CityRepository()

    private val _uiState = MutableStateFlow(
        CityUiState(sehirler = repository.sehirAra(""))
    )
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    fun aramaDegisti(yeniMetin: String) {
        _uiState.update { mevcut ->
            mevcut.copy(
                aramaMetni = yeniMetin,
                sehirler = repository.sehirAra(yeniMetin)
            )
        }
    }

    fun sehirSecildi(sehir: Sehir) {
        _uiState.update { mevcut ->
            mevcut.copy(seciliSehir = sehir.ad)
        }
        Log.d("SehirListesi", "Tıklandı: ${sehir.ad}")
    }
}