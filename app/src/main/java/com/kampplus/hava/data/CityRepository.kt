package com.kampplus.hava.data

import com.kampplus.hava.model.City

class CityRepository(
    private val api: CityApi = ApiClient.cityApi
) {
    suspend fun search(sorgu: String): List<City> {
        val response = api.search(name = sorgu, count = 20, language = "tr")
        return response.results ?: emptyList()
    }
}