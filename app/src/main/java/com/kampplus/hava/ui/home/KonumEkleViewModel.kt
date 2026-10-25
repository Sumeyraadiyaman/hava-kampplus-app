package com.kampplus.hava.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.KonumlarimRepository
import com.kampplus.hava.data.SonKonum
import com.kampplus.hava.data.mesafeKm
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Plakalar
import com.kampplus.hava.model.toKayitliKonum
import com.kampplus.hava.ui.LoadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class KonumEkleUiState(
    val metin: String = "",
    val ilOnerileri: List<String> = emptyList(),
    val aramaSonucu: LoadState<List<City>>? = null,
    val mesafeler: Map<Long, Int> = emptyMap(),
    val ekleniyor: Boolean = false,
    val eklendi: Boolean = false,
    val hata: String? = null
)

class KonumEkleViewModel(application: Application) : AndroidViewModel(application) {

    private val cityRepository = CityRepository()
    private val konumlarimRepository = KonumlarimRepository(application)

    private val _uiState = MutableStateFlow(KonumEkleUiState())
    val uiState: StateFlow<KonumEkleUiState> = _uiState.asStateFlow()

    private var aramaJob: Job? = null

    fun metinDegisti(yeniMetin: String) {
        aramaJob?.cancel()
        _uiState.update { mevcut ->
            mevcut.copy(
                metin = yeniMetin,
                ilOnerileri = Plakalar.ilAra(yeniMetin),
                aramaSonucu = null,
                hata = null
            )
        }
    }

    fun ara() {
        val sorgu = _uiState.value.metin.trim()
        if (sorgu.length < 2) return

        aramaJob?.cancel()
        aramaJob = viewModelScope.launch {
            _uiState.update { mevcut -> mevcut.copy(aramaSonucu = LoadState.Loading) }

            val yeniDurum: LoadState<List<City>> = try {
                val sehirler = sirala(cityRepository.search(sorgu))
                if (sehirler.isEmpty()) LoadState.Empty else LoadState.Success(sehirler)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("KonumEkle", "Arama hatası", e)
                LoadState.Error("Bağlantı kurulamadı. İnternetini kontrol et.")
            }

            _uiState.update { mevcut -> mevcut.copy(aramaSonucu = yeniDurum) }
        }
    }

    fun ilSecildi(il: String) {
        if (_uiState.value.ekleniyor) return

        viewModelScope.launch {
            _uiState.update { mevcut -> mevcut.copy(ekleniyor = true, hata = null) }
            try {
                val plaka = Plakalar.bul(il)
                val adaylar = cityRepository.search(il).filter { city -> city.countryCode == "TR" }
                val secilen = adaylar.firstOrNull { city -> Plakalar.bul(city.admin1) == plaka }
                    ?: adaylar.firstOrNull()

                if (secilen == null) {
                    _uiState.update { mevcut ->
                        mevcut.copy(ekleniyor = false, hata = "$il bulunamadı, tekrar dene.")
                    }
                } else {
                    konumlarimRepository.ekle(secilen.toKayitliKonum())
                    _uiState.update { mevcut -> mevcut.copy(ekleniyor = false, eklendi = true) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("KonumEkle", "İl ekleme hatası", e)
                _uiState.update { mevcut ->
                    mevcut.copy(ekleniyor = false, hata = "Bağlantı kurulamadı. İnternetini kontrol et.")
                }
            }
        }
    }

    fun sehirSecildi(city: City) {
        if (_uiState.value.ekleniyor) return

        viewModelScope.launch {
            _uiState.update { mevcut -> mevcut.copy(ekleniyor = true) }
            konumlarimRepository.ekle(city.toKayitliKonum())
            _uiState.update { mevcut -> mevcut.copy(ekleniyor = false, eklendi = true) }
        }
    }

    private fun sirala(sehirler: List<City>): List<City> {
        val konum = SonKonum.deger ?: return sehirler

        val mesafeler = sehirler.associate { city ->
            city.id to mesafeKm(
                konum.latitude, konum.longitude,
                city.latitude, city.longitude
            ).roundToInt()
        }
        _uiState.update { mevcut -> mevcut.copy(mesafeler = mesafeler) }

        return sehirler.sortedWith(
            compareBy<City>(
                { city -> city.countryCode != konum.ulkeKodu },
                { city -> mesafeler[city.id] }
            )
        )
    }
}