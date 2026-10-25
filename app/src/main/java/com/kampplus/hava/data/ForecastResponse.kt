package com.kampplus.hava.data

import com.google.gson.annotations.SerializedName
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyWeather

data class ForecastResponse(
    val current: CurrentDto?,
    val hourly: HourlyDto?,
    val daily: DailyDto?
)

data class CurrentDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Int?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("surface_pressure") val pressure: Double?,
    @SerializedName("is_day") val isDay: Int?
)

data class HourlyDto(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double?>,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double?>,
    @SerializedName("weather_code") val weatherCode: List<Int?>,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double?>,
    @SerializedName("relative_humidity_2m") val humidity: List<Int?>,
    @SerializedName("surface_pressure") val pressure: List<Double?>
)

data class DailyDto(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int?>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double?>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double?>,
    @SerializedName("precipitation_probability_max") val precipitationProbability: List<Int?>,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double?>,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double?>,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double?>,
    val sunrise: List<String?>,
    val sunset: List<String?>
)

fun ForecastResponse.toForecast(): Forecast {
    val anlik = current ?: error("Anlık hava verisi gelmedi")
    val saatlik = hourly ?: error("Saatlik tahmin verisi gelmedi")
    val gunluk = daily ?: error("Günlük tahmin verisi gelmedi")

    val saatler = saatlik.time.indices.map { i ->
        HourlyWeather(
            time = saatlik.time[i],
            temperature = saatlik.temperature.getOrNull(i) ?: 0.0,
            apparentTemperature = saatlik.apparentTemperature.getOrNull(i) ?: 0.0,
            weatherCode = saatlik.weatherCode.getOrNull(i) ?: 0,
            precipitationProbability = saatlik.precipitationProbability.getOrNull(i) ?: 0,
            windSpeed = saatlik.windSpeed.getOrNull(i) ?: 0.0,
            humidity = saatlik.humidity.getOrNull(i) ?: 0,
            pressure = saatlik.pressure.getOrNull(i) ?: 0.0
        )
    }

    val gunler = gunluk.time.indices.map { i ->
        DailyWeather(
            date = gunluk.time[i],
            weatherCode = gunluk.weatherCode.getOrNull(i) ?: 0,
            tempMax = gunluk.tempMax.getOrNull(i) ?: 0.0,
            tempMin = gunluk.tempMin.getOrNull(i) ?: 0.0,
            precipitationProbability = gunluk.precipitationProbability.getOrNull(i) ?: 0,
            precipitationSum = gunluk.precipitationSum.getOrNull(i) ?: 0.0,
            windSpeedMax = gunluk.windSpeedMax.getOrNull(i) ?: 0.0,
            uvIndexMax = gunluk.uvIndexMax.getOrNull(i) ?: 0.0,
            sunrise = gunluk.sunrise.getOrNull(i) ?: "",
            sunset = gunluk.sunset.getOrNull(i) ?: ""
        )
    }

    return Forecast(
        current = CurrentWeather(
            temperature = anlik.temperature ?: 0.0,
            apparentTemperature = anlik.apparentTemperature ?: 0.0,
            weatherCode = anlik.weatherCode ?: 0,
            windSpeed = anlik.windSpeed ?: 0.0,
            windDirection = anlik.windDirection ?: 0,
            humidity = anlik.humidity ?: 0,
            pressure = anlik.pressure ?: 0.0,
            isDay = anlik.isDay == 1
        ),
        hourly = saatler,
        daily = gunler
    )
}