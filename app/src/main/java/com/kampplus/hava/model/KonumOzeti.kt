package com.kampplus.hava.model

/** Konumlarım listesindeki kartlar için kısa hava özeti. */
data class KonumOzeti(
    val sicaklik: Double,
    val enYuksek: Double,
    val enDusuk: Double,
    val havaKodu: Int,
    val gunduz: Boolean,
    val zaman: String
)
