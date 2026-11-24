package com.example.weathernow.data.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results")
    val results: List<GeocodingItem>? = null
)

data class GeocodingItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("country_code")
    val countryCode: String? = null,
    @SerializedName("admin1")
    val admin1: String? = null,
    @SerializedName("timezone")
    val timezone: String? = null
)

data class CityLocation(
    val id: Long,
    val name: String,
    val country: String,
    val stateOrRegion: String,
    val latitude: Double,
    val longitude: Double
) {
    val displaySubtitle: String
        get() = listOfNotNull(
            stateOrRegion.takeIf { it.isNotBlank() && it != name },
            country.takeIf { it.isNotBlank() }
        ).joinToString(", ")
}
