package com.kampplus.hava.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.KonumlarimRepository
import com.kampplus.hava.data.SonKonum
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.KayitliKonum
import com.kampplus.hava.model.KonumOzeti
import com.kampplus.hava.model.toKayitliKonum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnaEkranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KonumlarimRepository(application)
    private val weatherRepository = WeatherRepository()

    val konumlar: StateFlow<List<KayitliKonum>> = repository.konumlar.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _gidilecekSayfa = MutableStateFlow<Int?>(null)
    val gidilecekSayfa: StateFlow<Int?> = _gidilecekSayfa.asStateFlow()

    /** Konumlarım kartlarında gösterilen kısa hava özetleri: konum id'si -> özet */
    private val _ozetler = MutableStateFlow<Map<String, KonumOzeti>>(emptyMap())
    val ozetler: StateFlow<Map<String, KonumOzeti>> = _ozetler.asStateFlow()

    private val yuklenenler = mutableSetOf<String>()

    init {
        // Yeni bir konum eklendiğinde onun özetini otomatik yükle
        viewModelScope.launch {
            repository.konumlar.collect { liste ->
                for (konum in liste) {
                    if (yuklenenler.add(konum.id)) {
                        launch { ozetYukle(konum.id, konum.latitude, konum.longitude) }
                    }
                }
            }
        }
    }

    fun konumumOzetiniYukle() {
        val konum = SonKonum.deger ?: return
        viewModelScope.launch {
            ozetYukle(KONUMUM_ANAHTARI, konum.latitude, konum.longitude)
        }
    }

    private suspend fun ozetYukle(anahtar: String, latitude: Double, longitude: Double) {
        try {
            val ozet = weatherRepository.ozet(latitude, longitude)
            _ozetler.update { mevcut -> mevcut + (anahtar to ozet) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("Konumlarim", "Özet alınamadı: $anahtar", e)
            yuklenenler.remove(anahtar)
        }
    }

    fun ekle(city: City) {
        viewModelScope.launch {
            repository.ekle(city.toKayitliKonum())
        }
    }

    fun sil(id: String) {
        viewModelScope.launch {
            repository.sil(id)
        }
    }

    /** yon = -1 yukarı, +1 aşağı taşır. */
    fun tasi(id: String, yon: Int) {
        viewModelScope.launch {
            repository.tasi(id, yon)
        }
    }

    fun sayfayaGit(sayfa: Int) {
        _gidilecekSayfa.value = sayfa
    }

    fun sayfayaGidildi() {
        _gidilecekSayfa.value = null
    }
}
