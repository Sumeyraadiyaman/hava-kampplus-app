package com.kampplus.hava.ui.forecast

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.KonumRepository
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
    val gunSayisi: Int = 7,
    val konumModu: Boolean = false,
    val izinGerekli: Boolean = false
)

class ForecastViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val gelenLatitude = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
    private val gelenLongitude = savedStateHandle.get<String>("lon")?.toDoubleOrNull()
    private val gelenAd = savedStateHandle.get<String>("name")

    private val konumModu = gelenLatitude == null || gelenLongitude == null

    private var latitude = gelenLatitude ?: 0.0
    private var longitude = gelenLongitude ?: 0.0
    private var konumBulundu = !konumModu

    private val repository = WeatherRepository()
    private val konumRepository = KonumRepository(application)

    private val _uiState = MutableStateFlow(
        ForecastUiState(sehirAdi = gelenAd ?: "", konumModu = konumModu)
    )
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        if (konumModu) konumlaBasla() else tahminiYukle()
    }

    fun tekrarDene() {
        if (konumBulundu) tahminiYukle() else konumlaBasla()
    }

    fun gunSayisiSecildi(gun: Int) {
        _uiState.update { mevcut -> mevcut.copy(gunSayisi = gun) }
    }

    fun izinSonucu(verildi: Boolean) {
        if (verildi) {
            konumlaBasla()
        } else {
            _uiState.update { mevcut ->
                mevcut.copy(
                    izinGerekli = true,
                    sehirAdi = "Konumum",
                    sonuc = LoadState.Error(
                        "Konum izni verilmedi. İzin verebilir ya da yukarıdaki 🔍 ile şehir arayabilirsin."
                    )
                )
            }
        }
    }

    private fun konumlaBasla() {
        if (!konumRepository.izinVarMi()) {
            _uiState.update { mevcut -> mevcut.copy(izinGerekli = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { mevcut ->
                mevcut.copy(
                    izinGerekli = false,
                    sehirAdi = "Konum bulunuyor…",
                    sonuc = LoadState.Loading
                )
            }

            val konum = try {
                konumRepository.mevcutKonumuAl()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Tahmin", "Konum hatası", e)
                null
            }

            if (konum == null) {
                _uiState.update { mevcut ->
                    mevcut.copy(
                        sehirAdi = "Konumum",
                        sonuc = LoadState.Error(
                            "Konum bulunamadı. Telefonunda konum servisinin açık olduğundan emin ol."
                        )
                    )
                }
            } else {
                latitude = konum.latitude
                longitude = konum.longitude
                konumBulundu = true
                _uiState.update { mevcut -> mevcut.copy(sehirAdi = konum.ad) }
                tahminiCek()
            }
        }
    }

    private fun tahminiYukle() {
        viewModelScope.launch {
            tahminiCek()
        }
    }

    private suspend fun tahminiCek() {
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