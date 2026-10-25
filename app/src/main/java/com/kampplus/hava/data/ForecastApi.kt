package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("daily") daily: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("timezone") timezone: String
    ): ForecastResponse
}