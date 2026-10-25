package com.kampplus.hava.data

import com.google.gson.annotations.SerializedName
import com.kampplus.hava.model.AirQuality
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.roundToInt

interface AirQualityApi {

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("timezone") timezone: String
    ): AirQualityResponse
}

data class AirQualityResponse(
    val current: AirQualityCurrentDto?,
    val hourly: AirQualityHourlyDto?
)

data class AirQualityCurrentDto(
    @SerializedName("european_aqi") val europeanAqi: Double?
)

data class AirQualityHourlyDto(
    val time: List<String>,
    @SerializedName("european_aqi") val europeanAqi: List<Double?>
)

fun AirQualityResponse.toAirQuality(): AirQuality {
    val gunlukEnYuksek = mutableMapOf<String, Int>()

    val saatlik = hourly
    if (saatlik != null) {
        for (i in saatlik.time.indices) {
            val deger = saatlik.europeanAqi.getOrNull(i) ?: continue
            val tarih = saatlik.time[i].take(10)
            val mevcut = gunlukEnYuksek[tarih]
            if (mevcut == null || deger.roundToInt() > mevcut) {
                gunlukEnYuksek[tarih] = deger.roundToInt()
            }
        }
    }

    return AirQuality(
        current = current?.europeanAqi?.roundToInt(),
        dailyMax = gunlukEnYuksek
    )
}