package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.WeatherCondition
import java.util.Calendar

enum class HavaEfekti { YOK, GUNES, SICAK, CICEK, YILDIZ, BULUT, SIS, YAGMUR, KAR, FIRTINA }

data class HavaGorunumu(
    val renkler: List<Color>,
    val efekt: HavaEfekti,
    val vurgu: Color
)

val varsayilanGorunum = HavaGorunumu(
    renkler = listOf(Color(0xFF2B5DA8), Color(0xFF4F86C6), Color(0xFF8DB7E3)),
    efekt = HavaEfekti.YOK,
    vurgu = Color(0xFF1565C0)
)

val menuGorunumu = HavaGorunumu(
    renkler = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF7E57C2)),
    efekt = HavaEfekti.YOK,
    vurgu = Color(0xFF3949AB)
)

fun mevcutAy(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

fun tarihtenAy(tarih: String): Int =
    if (tarih.length >= 7) tarih.substring(5, 7).toIntOrNull() ?: mevcutAy() else mevcutAy()

fun havaGorunumu(
    kosul: WeatherCondition,
    gunduz: Boolean,
    sicaklik: Double? = null,
    ay: Int = mevcutAy()
): HavaGorunumu {
    val ilkbahar = ay in 3..5
    val sicak = (sicaklik ?: 0.0) >= 27.0

    return when (kosul) {
        WeatherCondition.CLEAR, WeatherCondition.MOSTLY_CLEAR -> when {
            !gunduz -> HavaGorunumu(
                listOf(Color(0xFF0B1026), Color(0xFF1B2A5C), Color(0xFF3A3F7A)),
                HavaEfekti.YILDIZ,
                Color(0xFF3949AB)
            )
            sicak -> HavaGorunumu(
                listOf(Color(0xFFD7263D), Color(0xFFF46036), Color(0xFFFFB347)),
                HavaEfekti.SICAK,
                Color(0xFFD84315)
            )
            ilkbahar -> HavaGorunumu(
                listOf(Color(0xFF3FA7E0), Color(0xFF7FD1C7), Color(0xFFF9E79F)),
                HavaEfekti.CICEK,
                Color(0xFF2E7D32)
            )
            else -> HavaGorunumu(
                listOf(Color(0xFF1E88E5), Color(0xFF4FB3F6), Color(0xFFFFC37A)),
                HavaEfekti.GUNES,
                Color(0xFF1565C0)
            )
        }

        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.UNKNOWN ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF3F7CC4), Color(0xFF6E9FD6), Color(0xFFA9C6E6)),
                HavaEfekti.BULUT,
                Color(0xFF1565C0)
            ) else HavaGorunumu(
                listOf(Color(0xFF141B33), Color(0xFF2B3557), Color(0xFF4B5577)),
                HavaEfekti.BULUT,
                Color(0xFF3949AB)
            )

        WeatherCondition.OVERCAST ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF55657A), Color(0xFF7A8BA0), Color(0xFFA3B1C2)),
                HavaEfekti.BULUT,
                Color(0xFF455A64)
            ) else HavaGorunumu(
                listOf(Color(0xFF1C2230), Color(0xFF2E3647), Color(0xFF454E61)),
                HavaEfekti.BULUT,
                Color(0xFF37474F)
            )

        WeatherCondition.FOG ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF6F7A86), Color(0xFF959FAA), Color(0xFFBCC4CC)),
                HavaEfekti.SIS,
                Color(0xFF546E7A)
            ) else HavaGorunumu(
                listOf(Color(0xFF2A2F36), Color(0xFF3E444C), Color(0xFF575E66)),
                HavaEfekti.SIS,
                Color(0xFF455A64)
            )

        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.SHOWERS ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF2C3E50), Color(0xFF46637D), Color(0xFF6E8CA6)),
                HavaEfekti.YAGMUR,
                Color(0xFF1E3A5F)
            ) else HavaGorunumu(
                listOf(Color(0xFF0F1A26), Color(0xFF1F3042), Color(0xFF34495E)),
                HavaEfekti.YAGMUR,
                Color(0xFF1E3A5F)
            )

        WeatherCondition.SNOW ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF3E6E9A), Color(0xFF6E9CC6), Color(0xFFA9CDEB)),
                HavaEfekti.KAR,
                Color(0xFF0277BD)
            ) else HavaGorunumu(
                listOf(Color(0xFF16243A), Color(0xFF2D4466), Color(0xFF557096)),
                HavaEfekti.KAR,
                Color(0xFF01579B)
            )

        WeatherCondition.THUNDERSTORM ->
            if (gunduz) HavaGorunumu(
                listOf(Color(0xFF1F2238), Color(0xFF383B5A), Color(0xFF575A7A)),
                HavaEfekti.FIRTINA,
                Color(0xFF4527A0)
            ) else HavaGorunumu(
                listOf(Color(0xFF0B0C17), Color(0xFF1B1C2E), Color(0xFF303149)),
                HavaEfekti.FIRTINA,
                Color(0xFF311B92)
            )
    }
}
