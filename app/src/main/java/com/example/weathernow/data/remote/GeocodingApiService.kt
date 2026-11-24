package com.example.weathernow.data.remote

import com.example.weathernow.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "id",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
