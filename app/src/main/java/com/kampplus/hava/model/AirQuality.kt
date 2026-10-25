package com.kampplus.hava.model

data class AirQuality(
    val current: Int?,
    val dailyMax: Map<String, Int>
)

enum class HavaKalitesiSeviyesi(val etiket: String, val emoji: String) {
    IYI("İyi", "🟢"),
    MAKUL("Makul", "🟡"),
    ORTA("Orta", "🟠"),
    KOTU("Kötü", "🔴"),
    COK_KOTU("Çok kötü", "🟣"),
    ASIRI_KOTU("Aşırı kötü", "🟤");

    companion object {
        fun fromAqi(aqi: Int): HavaKalitesiSeviyesi = when {
            aqi < 20 -> IYI
            aqi < 40 -> MAKUL
            aqi < 60 -> ORTA
            aqi < 80 -> KOTU
            aqi < 100 -> COK_KOTU
            else -> ASIRI_KOTU
        }
    }
}