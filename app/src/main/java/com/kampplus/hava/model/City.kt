package com.kampplus.hava.model

import com.google.gson.annotations.SerializedName

data class City(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    @SerializedName("country_code") val countryCode: String?,
    val admin1: String?
)

data class CitySearchResponse(
    val results: List<City>?
)