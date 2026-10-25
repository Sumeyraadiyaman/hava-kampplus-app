package com.kampplus.hava.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.KayitliKonum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.konumlarDataStore by preferencesDataStore(name = "konumlarim")

class KonumlarimRepository(private val context: Context) {

    private val anahtar = stringPreferencesKey("kayitli_konumlar")
    private val gson = Gson()
    private val listeTipi = object : TypeToken<List<KayitliKonum>>() {}.type

    val konumlar: Flow<List<KayitliKonum>> =
        context.konumlarDataStore.data.map { tercihler ->
            jsondanListeye(tercihler[anahtar])
        }

    suspend fun ekle(konum: KayitliKonum) {
        context.konumlarDataStore.edit { tercihler ->
            val mevcut = jsondanListeye(tercihler[anahtar])
            if (mevcut.none { kayitli -> kayitli.id == konum.id }) {
                tercihler[anahtar] = gson.toJson(mevcut + konum)
            }
        }
    }

    suspend fun sil(id: String) {
        context.konumlarDataStore.edit { tercihler ->
            val mevcut = jsondanListeye(tercihler[anahtar])
            tercihler[anahtar] = gson.toJson(mevcut.filterNot { kayitli -> kayitli.id == id })
        }
    }

    private fun jsondanListeye(json: String?): List<KayitliKonum> {
        if (json == null) return emptyList()
        return try {
            gson.fromJson<List<KayitliKonum>>(json, listeTipi) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}