package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.WeatherCondition

enum class HavaEfekti { YOK, GUNES, YILDIZ, BULUT, SIS, YAGMUR, KAR, FIRTINA }

data class HavaGorunumu(
    val renkler: List<Color>,
    val efekt: HavaEfekti
)

val varsayilanGorunum = HavaGorunumu(
    renkler = listOf(Color(0xFF3F7CC4), Color(0xFF7FA9DA)),
    efekt = HavaEfekti.YOK
)

fun havaGorunumu(kosul: WeatherCondition, gunduz: Boolean): HavaGorunumu = when (kosul) {
    WeatherCondition.CLEAR, WeatherCondition.MOSTLY_CLEAR ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF1E88E5), Color(0xFF64B5F6), Color(0xFFFFCC80)),
            HavaEfekti.GUNES
        ) else HavaGorunumu(
            listOf(Color(0xFF0B1026), Color(0xFF1B2A5C), Color(0xFF3A3F7A)),
            HavaEfekti.YILDIZ
        )

    WeatherCondition.PARTLY_CLOUDY, WeatherCondition.UNKNOWN ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF3F7CC4), Color(0xFF7FA9DA), Color(0xFFB9CFE8)),
            HavaEfekti.BULUT
        ) else HavaGorunumu(
            listOf(Color(0xFF141B33), Color(0xFF2B3557), Color(0xFF4B5577)),
            HavaEfekti.BULUT
        )

    WeatherCondition.OVERCAST ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF5E6E80), Color(0xFF8595A6), Color(0xFFAEBAC6)),
            HavaEfekti.BULUT
        ) else HavaGorunumu(
            listOf(Color(0xFF1C2230), Color(0xFF2E3647), Color(0xFF454E61)),
            HavaEfekti.BULUT
        )

    WeatherCondition.FOG ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF7D8894), Color(0xFFA3ADB7), Color(0xFFC9D0D6)),
            HavaEfekti.SIS
        ) else HavaGorunumu(
            listOf(Color(0xFF2A2F36), Color(0xFF3E444C), Color(0xFF575E66)),
            HavaEfekti.SIS
        )

    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.SHOWERS ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF34495E), Color(0xFF51708A), Color(0xFF7896AE)),
            HavaEfekti.YAGMUR
        ) else HavaGorunumu(
            listOf(Color(0xFF0F1A26), Color(0xFF1F3042), Color(0xFF34495E)),
            HavaEfekti.YAGMUR
        )

    WeatherCondition.SNOW ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF4F7EA8), Color(0xFF7FA8CC), Color(0xFFB4D2EA)),
            HavaEfekti.KAR
        ) else HavaGorunumu(
            listOf(Color(0xFF16243A), Color(0xFF2D4466), Color(0xFF557096)),
            HavaEfekti.KAR
        )

    WeatherCondition.THUNDERSTORM ->
        if (gunduz) HavaGorunumu(
            listOf(Color(0xFF23263F), Color(0xFF3C3F5E), Color(0xFF5A5D7C)),
            HavaEfekti.FIRTINA
        ) else HavaGorunumu(
            listOf(Color(0xFF0B0C17), Color(0xFF1B1C2E), Color(0xFF303149)),
            HavaEfekti.FIRTINA
        )
}