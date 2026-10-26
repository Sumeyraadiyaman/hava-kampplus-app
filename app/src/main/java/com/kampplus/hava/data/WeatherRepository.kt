package com.kampplus.hava.data

import android.util.Log
import com.kampplus.hava.model.AirQuality
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.KonumOzeti
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

        tahmin.copy(airQuality = havaKalitesi)
    }

    /** Konumlarım kartları için hafif, tek günlük özet. */
    suspend fun ozet(latitude: Double, longitude: Double): KonumOzeti {
        val response = api.getForecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code,is_day",
            hourly = "temperature_2m",
            daily = "temperature_2m_max,temperature_2m_min",
            forecastDays = 1,
            timezone = "auto"
        )
        val anlik = response.current ?: error("Anlık hava verisi gelmedi")
        val gunluk = response.daily

        return KonumOzeti(
            sicaklik = anlik.temperature ?: 0.0,
            enYuksek = gunluk?.tempMax?.firstOrNull() ?: 0.0,
            enDusuk = gunluk?.tempMin?.firstOrNull() ?: 0.0,
            havaKodu = anlik.weatherCode ?: 0,
            gunduz = anlik.isDay == 1,
            zaman = anlik.time ?: ""
        )
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
