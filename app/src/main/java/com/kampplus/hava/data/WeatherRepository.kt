package com.kampplus.hava.data

import com.kampplus.hava.model.Forecast

class WeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi
) {
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        val response = api.getForecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,apparent_temperature,weather_code,wind_speed_10m,relative_humidity_2m",
            daily = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,uv_index_max",
            forecastDays = 16,
            timezone = "auto"
        )
        return response.toForecast()
    }
}