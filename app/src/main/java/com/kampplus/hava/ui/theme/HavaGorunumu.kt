package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.WeatherCondition

enum class HavaEfekti {
    YOK, GUNES, SICAK, ILKBAHAR, SONBAHAR, YILDIZ, BULUT, SIS, YAGMUR, KAR, FIRTINA
}

data class HavaGorunumu(
    val renkler: List<Color>,
    val efekt: HavaEfekti
)

val varsayilanGorunum = HavaGorunumu(
    renkler = listOf(Color(0xFF2F5FA7), Color(0xFF5B8ED6), Color(0xFF9CC0EE)),
    efekt = HavaEfekti.YOK
)

val menuGorunumu = HavaGorunumu(
    renkler = listOf(Color(0xFF241A55), Color(0xFF4A3A9C), Color(0xFF8A6FD8)),
    efekt = HavaEfekti.YILDIZ
)

/** "2026-10-25T17:30" ya da "2026-10-25" metninden ayı (1-12) çıkarır. */
fun ayBul(tarih: String): Int = tarih.drop(5).take(2).toIntOrNull() ?: 1

fun havaGorunumu(
    kosul: WeatherCondition,
    gunduz: Boolean,
    ay: Int = 1,
    sicaklik: Double = 15.0
): HavaGorunumu {
    val acikHava = kosul == WeatherCondition.CLEAR ||
        kosul == WeatherCondition.MOSTLY_CLEAR ||
        kosul == WeatherCondition.PARTLY_CLOUDY

    if (acikHava && gunduz) {
        return when {
            sicaklik >= 27 -> HavaGorunumu(
                listOf(Color(0xFFC62828), Color(0xFFEF6C3A), Color(0xFFFFB04F)),
                HavaEfekti.SICAK
            )
            ay in 3..5 -> HavaGorunumu(
                listOf(Color(0xFF2E9BEA), Color(0xFF74C7F2), Color(0xFFB5E3A0)),
                HavaEfekti.ILKBAHAR
            )
            ay in 9..11 -> HavaGorunumu(
                listOf(Color(0xFF2B5F96), Color(0xFF6E98C6), Color(0xFFE8995A)),
                HavaEfekti.SONBAHAR
            )
            else -> HavaGorunumu(
                listOf(Color(0xFF1976D2), Color(0xFF55A8EE), Color(0xFFFFCC80)),
                HavaEfekti.GUNES
            )
        }
    }

    return when (kosul) {
        WeatherCondition.CLEAR,
        WeatherCondition.MOSTLY_CLEAR,
        WeatherCondition.PARTLY_CLOUDY -> HavaGorunumu(
            listOf(Color(0xFF15103A), Color(0xFF34267A), Color(0xFF6A4FB8)),
            HavaEfekti.YILDIZ
        )

        WeatherCondition.OVERCAST,
        WeatherCondition.UNKNOWN ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF4F5F72), Color(0xFF7A8A9C), Color(0xFFA9B6C4)),
                HavaEfekti.BULUT
            ) else HavaGorunumu(
                listOf(Color(0xFF1C1838), Color(0xFF332D5C), Color(0xFF55507F)),
                HavaEfekti.BULUT
            )

        WeatherCondition.FOG ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF6E7A86), Color(0xFF95A0AB), Color(0xFFBEC6CE)),
                HavaEfekti.SIS
            ) else HavaGorunumu(
                listOf(Color(0xFF23282F), Color(0xFF373D45), Color(0xFF50575F)),
                HavaEfekti.SIS
            )

        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.SHOWERS ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF2C3E50), Color(0xFF486580), Color(0xFF6F8DA8)),
                HavaEfekti.YAGMUR
            ) else HavaGorunumu(
                listOf(Color(0xFF121436), Color(0xFF272B63), Color(0xFF474D8E)),
                HavaEfekti.YAGMUR
            )

        WeatherCondition.SNOW ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF3D6E9C), Color(0xFF6F9CC6), Color(0xFFA9CDEA)),
                HavaEfekti.KAR
            ) else HavaGorunumu(
                listOf(Color(0xFF111D30), Color(0xFF26405F), Color(0xFF4E6C92)),
                HavaEfekti.KAR
            )

        WeatherCondition.THUNDERSTORM ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF1E2038), Color(0xFF363958), Color(0xFF555878)),
                HavaEfekti.FIRTINA
            ) else HavaGorunumu(
                listOf(Color(0xFF08090F), Color(0xFF171828), Color(0xFF2B2C44)),
                HavaEfekti.FIRTINA
            )
    }
}
