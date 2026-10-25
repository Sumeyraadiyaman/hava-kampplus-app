package com.kampplus.hava.data

import com.kampplus.hava.model.Forecast

class WeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi
) {
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        val response = api.getForecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,relative_humidity_2m,surface_pressure,is_day",
            hourly = "temperature_2m,apparent_temperature,weather_code,precipitation_probability,wind_speed_10m,relative_humidity_2m,surface_pressure",
            daily = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum,wind_speed_10m_max,uv_index_max,sunrise,sunset",
            forecastDays = 16,
            timezone = "auto"
        )
        return response.toForecast()
    }
}