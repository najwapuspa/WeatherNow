package com.example.weathernow.data.repository

import com.example.weathernow.data.model.CityLocation
import com.example.weathernow.data.model.CurrentWeather
import com.example.weathernow.data.model.DailyForecastItem
import com.example.weathernow.data.model.HourlyForecastItem
import com.example.weathernow.data.model.WeatherDashboardData
import com.example.weathernow.data.remote.GeocodingApiService
import com.example.weathernow.data.remote.RetrofitClient
import com.example.weathernow.data.remote.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(
    private val geocodingApi: GeocodingApiService = RetrofitClient.geocodingApi,
    private val weatherApi: WeatherApiService = RetrofitClient.weatherApi
) {

    private val idLocale = Locale.forLanguageTag("id-ID")

    suspend fun searchCities(query: String): Result<List<CityLocation>> = withContext(Dispatchers.IO) {
        try {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val response = geocodingApi.searchCity(name = trimmed, language = "id")
            val items = response.results.orEmpty().map { item ->
                CityLocation(
                    id = item.id,
                    name = item.name,
                    country = item.country.orEmpty(),
                    stateOrRegion = item.admin1.orEmpty(),
                    latitude = item.latitude,
                    longitude = item.longitude
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeather(
        city: CityLocation,
        isFavorite: Boolean = false
    ): Result<WeatherDashboardData> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )

            val currentDto = response.current
                ?: return@withContext Result.failure(IllegalStateException("Data cuaca saat ini tidak tersedia"))

            val currentWeather = CurrentWeather(
                temperature = currentDto.temperature2m,
                feelsLike = currentDto.apparentTemperature,
                humidity = currentDto.relativeHumidity2m,
                windSpeed = currentDto.windSpeed10m,
                weatherCode = currentDto.weatherCode,
                conditionDescription = WeatherCodeHelper.getDescription(currentDto.weatherCode),
                time = currentDto.time
            )

            // --- Parse Daily Forecast ---
            val dailyDto = response.daily
            val dailyList = mutableListOf<DailyForecastItem>()

            if (dailyDto != null && dailyDto.time.isNotEmpty()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEEE", idLocale)
                val dateFormat = SimpleDateFormat("d MMMM yyyy", idLocale)

                for (i in dailyDto.time.indices) {
                    val dateStr = dailyDto.time[i]
                    val code = dailyDto.weatherCode.getOrNull(i) ?: 0
                    val maxTemp = dailyDto.temperature2mMax.getOrNull(i) ?: 0.0
                    val minTemp = dailyDto.temperature2mMin.getOrNull(i) ?: 0.0

                    var dayName: String
                    var formattedDate: String
                    try {
                        val parsedDate: Date? = inputFormat.parse(dateStr)
                        if (parsedDate != null) {
                            dayName = when (i) {
                                0 -> "Hari Ini"
                                1 -> "Besok"
                                else -> dayFormat.format(parsedDate)
                            }
                            formattedDate = dateFormat.format(parsedDate)
                        } else {
                            dayName = if (i == 0) "Hari Ini" else "Hari ke-${i + 1}"
                            formattedDate = dateStr
                        }
                    } catch (_: Exception) {
                        dayName = if (i == 0) "Hari Ini" else "Hari ke-${i + 1}"
                        formattedDate = dateStr
                    }

                    dailyList.add(
                        DailyForecastItem(
                            dateString = dateStr,
                            dayName = dayName,
                            formattedDate = formattedDate,
                            weatherCode = code,
                            conditionDescription = WeatherCodeHelper.getDescription(code),
                            tempMax = maxTemp,
                            tempMin = minTemp,
                            isToday = i == 0
                        )
                    )
                }
            }

            // --- Parse Hourly Forecast (Next 24 Hours) ---
            val hourlyDto = response.hourly
            val hourlyList = mutableListOf<HourlyForecastItem>()

            if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
                val hourInputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val hourOutputFormat = SimpleDateFormat("HH:mm", idLocale)
                val currentHourString = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.getDefault()).format(Date())

                // Find index matching current hour or start from 0
                var startIndex = 0
                for (i in hourlyDto.time.indices) {
                    if (hourlyDto.time[i].startsWith(currentHourString)) {
                        startIndex = i
                        break
                    }
                }

                val endIndex = (startIndex + 24).coerceAtMost(hourlyDto.time.size)
                for (i in startIndex until endIndex) {
                    val timeStr = hourlyDto.time[i]
                    val temp = hourlyDto.temperature2m.getOrNull(i) ?: 0.0
                    val code = hourlyDto.weatherCode.getOrNull(i) ?: 0

                    val label = if (i == startIndex) {
                        "Sekarang"
                    } else {
                        try {
                            val parsed = hourInputFormat.parse(timeStr)
                            if (parsed != null) hourOutputFormat.format(parsed) else timeStr.takeLast(5)
                        } catch (_: Exception) {
                            timeStr.takeLast(5)
                        }
                    }

                    hourlyList.add(
                        HourlyForecastItem(
                            timeString = timeStr,
                            hourLabel = label,
                            temperature = temp,
                            weatherCode = code,
                            conditionDescription = WeatherCodeHelper.getDescription(code),
                            isNow = i == startIndex
                        )
                    )
                }
            }

            val now = Date()
            val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", idLocale)
            val timeFormat = SimpleDateFormat("HH:mm", idLocale)

            val localFormattedDate = fullDateFormat.format(now)
            val lastUpdatedTime = "${timeFormat.format(now)} WIB"

            val dashboardData = WeatherDashboardData(
                city = city,
                current = currentWeather,
                dailyForecast = dailyList,
                hourlyForecast = hourlyList,
                localFormattedDate = localFormattedDate,
                lastUpdated = lastUpdatedTime,
                isFavorite = isFavorite
            )

            Result.success(dashboardData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val DEFAULT_CITY = CityLocation(
            id = 1642911L,
            name = "Jakarta",
            country = "Indonesia",
            stateOrRegion = "DKI Jakarta",
            latitude = -6.21462,
            longitude = 106.84513
        )
    }
}
