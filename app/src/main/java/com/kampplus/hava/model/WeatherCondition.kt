package com.kampplus.hava.model

enum class WeatherCondition(val emoji: String, val label: String) {
    CLEAR("☀️", "Açık"),
    MOSTLY_CLEAR("🌤️", "Az bulutlu"),
    PARTLY_CLOUDY("⛅", "Parçalı bulutlu"),
    OVERCAST("☁️", "Kapalı"),
    FOG("🌫️", "Sisli"),
    DRIZZLE("🌦️", "Çisenti"),
    RAIN("🌧️", "Yağmurlu"),
    SHOWERS("🌦️", "Sağanak"),
    SNOW("❄️", "Karlı"),
    THUNDERSTORM("⛈️", "Gök gürültülü fırtına"),
    UNKNOWN("❔", "Bilinmiyor");

    companion object {
        fun fromWeatherCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1 -> MOSTLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67 -> RAIN
            80, 81, 82 -> SHOWERS
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}