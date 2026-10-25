package com.kampplus.hava.ui.city

import com.kampplus.hava.data.SonKonum
import com.kampplus.hava.data.mesafeKm
import kotlin.math.roundToInt
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CityUiState(
    val aramaMetni: String = "",
    val sonuc: LoadState<List<City>> = LoadState.Empty,
    val seciliSehirId: Long? = null,
    val mesafeler: Map<Long, Int> = emptyMap()
)

class CityViewModel : ViewModel() {

    private val repository = CityRepository()

    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    private var aramaJob: Job? = null

    fun aramaDegisti(yeniMetin: String) {
        _uiState.update { mevcut -> mevcut.copy(aramaMetni = yeniMetin) }

        aramaJob?.cancel()
        val sorgu = yeniMetin.trim()
        if (sorgu.length < 3) {
            _uiState.update { mevcut -> mevcut.copy(sonuc = LoadState.Empty) }
            return
        }

        _uiState.update { mevcut -> mevcut.copy(sonuc = LoadState.Loading) }
        aramaJob = viewModelScope.launch {
            delay(400)
            ara(sorgu)
        }
    }

    fun tekrarDene() {
        aramaJob?.cancel()
        val sorgu = _uiState.value.aramaMetni.trim()
        aramaJob = viewModelScope.launch {
            ara(sorgu)
        }
    }

    fun sehirSecildi(city: City) {
        _uiState.update { mevcut -> mevcut.copy(seciliSehirId = city.id) }
        Log.d("SehirListesi", "Tıklandı: ${city.name}")
    }

    private suspend fun ara(sorgu: String) {
        _uiState.update { mevcut -> mevcut.copy(sonuc = LoadState.Loading) }

        var mesafeler = emptyMap<Long, Int>()

        val yeniDurum = try {
            val sehirler = repository.search(sorgu)
            val konum = SonKonum.deger

            val siralanmis = if (konum == null) {
                sehirler
            } else {
                mesafeler = sehirler.associate { city ->
                    city.id to mesafeKm(
                        konum.latitude, konum.longitude,
                        city.latitude, city.longitude
                    ).roundToInt()
                }
                sehirler.sortedWith(
                    compareBy<City>(
                        { city -> city.countryCode != konum.ulkeKodu },
                        { city -> mesafeler[city.id] }
                    )
                )
            }

            if (siralanmis.isEmpty()) {
                LoadState.Empty
            } else {
                LoadState.Success(siralanmis)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SehirListesi", "Arama hatası", e)
            LoadState.Error("Bağlantı kurulamadı. İnternetini kontrol et.")
        }

        _uiState.update { mevcut -> mevcut.copy(sonuc = yeniDurum, mesafeler = mesafeler) }
    }
}