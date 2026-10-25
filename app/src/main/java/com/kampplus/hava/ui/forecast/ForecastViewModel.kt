package com.kampplus.hava.ui.forecast

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.ui.LoadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForecastUiState(
    val sehirAdi: String = "",
    val sonuc: LoadState<Forecast> = LoadState.Loading,
    val gunSayisi: Int = 7
)

class ForecastViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val latitude = savedStateHandle.get<String>("lat")?.toDoubleOrNull() ?: 0.0
    private val longitude = savedStateHandle.get<String>("lon")?.toDoubleOrNull() ?: 0.0
    private val sehirAdi = savedStateHandle.get<String>("name") ?: ""

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(ForecastUiState(sehirAdi = sehirAdi))
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        yukle()
    }

    fun tekrarDene() {
        yukle()
    }

    fun gunSayisiSecildi(gun: Int) {
        _uiState.update { mevcut -> mevcut.copy(gunSayisi = gun) }
    }

    private fun yukle() {
        viewModelScope.launch {
            _uiState.update { mevcut -> mevcut.copy(sonuc = LoadState.Loading) }

            val yeniDurum: LoadState<Forecast> = try {
                LoadState.Success(repository.getForecast(latitude, longitude))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Tahmin", "Tahmin hatası", e)
                LoadState.Error("Hava durumu alınamadı. İnternetini kontrol et.")
            }

            _uiState.update { mevcut -> mevcut.copy(sonuc = yeniDurum) }
        }
    }
}