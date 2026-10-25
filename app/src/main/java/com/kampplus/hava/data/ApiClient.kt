package com.kampplus.hava.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"

    val cityApi: CityApi = Retrofit.Builder()
        .baseUrl(GEOCODING_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CityApi::class.java)

    val forecastApi: ForecastApi = Retrofit.Builder()
        .baseUrl(FORECAST_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForecastApi::class.java)
}