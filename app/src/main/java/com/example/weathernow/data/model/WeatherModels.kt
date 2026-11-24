package com.example.weathernow.data.model

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

// --- Settings & Preferences Enums ---
enum class ThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class TemperatureUnit(val symbol: String, val title: String) {
    CELSIUS("°C", "Celsius (°C)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)")
}

fun Double.toDisplayTemp(unit: TemperatureUnit): Int {
    return if (unit == TemperatureUnit.FAHRENHEIT) {
        ((this * 9.0 / 5.0) + 32.0).roundToInt()
    } else {
        this.roundToInt()
    }
}

// --- Forecast API Models ---
data class ForecastResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("current")
    val current: CurrentWeatherDto? = null,
    @SerializedName("daily")
    val daily: DailyWeatherDto? = null,
    @SerializedName("hourly")
    val hourly: HourlyWeatherDto? = null
)

data class CurrentWeatherDto(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temperature2m: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double
)

data class DailyWeatherDto(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double>
)

data class HourlyWeatherDto(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)

// --- Domain Models for Clean UI State ---
data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val time: String
)

data class DailyForecastItem(
    val dateString: String,
    val dayName: String,
    val formattedDate: String,
    val weatherCode: Int,
    val conditionDescription: String,
    val tempMax: Double,
    val tempMin: Double,
    val isToday: Boolean = false
)

data class HourlyForecastItem(
    val timeString: String,
    val hourLabel: String,
    val temperature: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val isNow: Boolean = false
)

data class WeatherDashboardData(
    val city: CityLocation,
    val current: CurrentWeather,
    val dailyForecast: List<DailyForecastItem>,
    val hourlyForecast: List<HourlyForecastItem>,
    val localFormattedDate: String,
    val lastUpdated: String,
    val isFavorite: Boolean = false
)

data class FavoriteCityWeather(
    val city: CityLocation,
    val currentTemp: Double? = null,
    val weatherCode: Int? = null,
    val conditionDescription: String? = null,
    val isLoading: Boolean = false
)
