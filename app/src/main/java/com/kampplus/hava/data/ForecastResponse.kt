package com.kampplus.hava.data

import com.google.gson.annotations.SerializedName
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.Forecast

data class ForecastResponse(
    val current: CurrentDto?,
    val daily: DailyDto?
)

data class CurrentDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?
)

data class DailyDto(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int?>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double?>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double?>,
    @SerializedName("precipitation_probability_max") val precipitationProbability: List<Int?>,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double?>,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double?>
)

fun ForecastResponse.toForecast(): Forecast {
    val anlik = current ?: error("Anlık hava verisi gelmedi")
    val gunluk = daily ?: error("Günlük tahmin verisi gelmedi")

    val gunler = gunluk.time.indices.map { i ->
        DailyWeather(
            date = gunluk.time[i],
            weatherCode = gunluk.weatherCode.getOrNull(i) ?: 0,
            tempMax = gunluk.tempMax.getOrNull(i) ?: 0.0,
            tempMin = gunluk.tempMin.getOrNull(i) ?: 0.0,
            precipitationProbability = gunluk.precipitationProbability.getOrNull(i) ?: 0,
            windSpeedMax = gunluk.windSpeedMax.getOrNull(i) ?: 0.0,
            uvIndexMax = gunluk.uvIndexMax.getOrNull(i) ?: 0.0
        )
    }

    return Forecast(
        current = CurrentWeather(
            temperature = anlik.temperature ?: 0.0,
            apparentTemperature = anlik.apparentTemperature ?: 0.0,
            weatherCode = anlik.weatherCode ?: 0,
            windSpeed = anlik.windSpeed ?: 0.0,
            humidity = anlik.humidity ?: 0
        ),
        daily = gunler
    )
}