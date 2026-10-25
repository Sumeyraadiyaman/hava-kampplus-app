package com.kampplus.hava.data

import android.util.Log
import com.kampplus.hava.model.AirQuality
import com.kampplus.hava.model.Forecast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val airQualityApi: AirQualityApi = ApiClient.airQualityApi
) {
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast = coroutineScope {
        val tahminIstegi = async { tahminiGetir(latitude, longitude) }
        val havaKalitesiIstegi = async { havaKalitesiniGetir(latitude, longitude) }

        val tahmin = tahminIstegi.await()
        val havaKalitesi = havaKalitesiIstegi.await()

        Log.d(
            "Tahmin",
            "Hava kalitesi: şu an=${havaKalitesi?.current}, gün sayısı=${havaKalitesi?.dailyMax?.size}"
        )

        tahmin.copy(airQuality = havaKalitesi)
    }

    private suspend fun tahminiGetir(latitude: Double, longitude: Double): Forecast {
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

    private suspend fun havaKalitesiniGetir(latitude: Double, longitude: Double): AirQuality? {
        return try {
            airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                current = "european_aqi",
                hourly = "european_aqi",
                forecastDays = 5,
                timezone = "auto"
            ).toAirQuality()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("Tahmin", "Hava kalitesi alınamadı", e)
            null
        }
    }
}