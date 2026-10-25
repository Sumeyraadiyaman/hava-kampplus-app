package com.kampplus.hava.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.KonumlarimRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.KayitliKonum
import com.kampplus.hava.model.toKayitliKonum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnaEkranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KonumlarimRepository(application)

    val konumlar: StateFlow<List<KayitliKonum>> = repository.konumlar.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

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
}