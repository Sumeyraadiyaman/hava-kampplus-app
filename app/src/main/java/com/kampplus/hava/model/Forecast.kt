package com.kampplus.hava.model

data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val humidity: Int,
    val pressure: Double,
    val isDay: Boolean
)

data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val windSpeed: Double,
    val humidity: Int,
    val pressure: Double
)

data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbability: Int,
    val precipitationSum: Double,
    val windSpeedMax: Double,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String
)