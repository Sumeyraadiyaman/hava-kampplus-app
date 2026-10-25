package com.kampplus.hava.data

import com.kampplus.hava.model.Sehir

class CityRepository {

    private val sehirler = listOf(
        Sehir("Adana", "01"),
        Sehir("Ankara", "06"),
        Sehir("Antalya", "07"),
        Sehir("Bursa", "16"),
        Sehir("Diyarbakır", "21"),
        Sehir("Elazığ", "23"),
        Sehir("Erzurum", "25"),
        Sehir("Eskişehir", "26"),
        Sehir("Gaziantep", "27"),
        Sehir("İstanbul", "34"),
        Sehir("İzmir", "35"),
        Sehir("Konya", "42"),
        Sehir("Malatya", "44"),
        Sehir("Samsun", "55"),
        Sehir("Trabzon", "61")
    )

    fun sehirAra(sorgu: String): List<Sehir> {
        return sehirler.filter { sehir ->
            sehir.ad.contains(sorgu, ignoreCase = true)
        }
    }
}