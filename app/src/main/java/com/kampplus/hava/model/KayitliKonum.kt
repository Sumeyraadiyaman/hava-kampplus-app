package com.kampplus.hava.model

import kotlin.math.roundToInt

data class KayitliKonum(
    val id: String,
    val ad: String,
    val altBilgi: String,
    val latitude: Double,
    val longitude: Double
)

fun City.toKayitliKonum(): KayitliKonum {
    val ulkeAdi = if (countryCode == "TR") "Türkiye" else country
    return KayitliKonum(
        id = "${(latitude * 1000).roundToInt()},${(longitude * 1000).roundToInt()}",
        ad = name,
        altBilgi = listOfNotNull(admin1, ulkeAdi).distinct().joinToString(", "),
        latitude = latitude,
        longitude = longitude
    )
}