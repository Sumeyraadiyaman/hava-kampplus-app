package com.kampplus.hava.model

data class Forecast(
    val current: CurrentWeather,
    val daily: List<DailyWeather>
)

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val humidity: Int
)

data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbability: Int,
    val windSpeedMax: Double,
    val uvIndexMax: Double
)