package com.example.weathernow

import com.example.weathernow.data.model.AppLanguage
import com.example.weathernow.data.model.CityLocation
import com.example.weathernow.data.model.ForecastResponse
import com.example.weathernow.data.model.GeocodingResponse
import com.example.weathernow.data.model.TemperatureUnit
import com.example.weathernow.data.model.toDisplayTemp
import com.example.weathernow.data.repository.WeatherCodeHelper
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherModelsTest {

    private val gson = Gson()

    @Test
    fun testWeatherCodeHelperDescriptions() {
        // Indonesian (default)
        val clearSkyId = WeatherCodeHelper.getDescription(0, AppLanguage.INDONESIAN)
        assertEquals("Cerah", clearSkyId)

        val rainId = WeatherCodeHelper.getDescription(61, AppLanguage.INDONESIAN)
        assertEquals("Hujan Ringan", rainId)

        val thunderId = WeatherCodeHelper.getDescription(95, AppLanguage.INDONESIAN)
        assertEquals("Badai Petir", thunderId)

        // English
        val clearSkyEn = WeatherCodeHelper.getDescription(0, AppLanguage.ENGLISH)
        assertEquals("Clear Sky", clearSkyEn)

        val rainEn = WeatherCodeHelper.getDescription(61, AppLanguage.ENGLISH)
        assertEquals("Slight Rain", rainEn)

        val thunderEn = WeatherCodeHelper.getDescription(95, AppLanguage.ENGLISH)
        assertEquals("Thunderstorm", thunderEn)
    }

    @Test
    fun testAppLanguageValues() {
        assertEquals("id", AppLanguage.INDONESIAN.code)
        assertEquals("en", AppLanguage.ENGLISH.code)
        assertEquals("Bahasa Indonesia", AppLanguage.INDONESIAN.title)
        assertEquals("English", AppLanguage.ENGLISH.title)
    }

    @Test
    fun testCityLocationDisplaySubtitle() {
        val city = CityLocation(
            id = 1,
            name = "Bandung",
            country = "Indonesia",
            stateOrRegion = "Jawa Barat",
            latitude = -6.9175,
            longitude = 107.6191
        )
        assertEquals("Jawa Barat, Indonesia", city.displaySubtitle)
    }

    @Test
    fun testTemperatureConversion() {
        assertEquals(0, 0.0.toDisplayTemp(TemperatureUnit.CELSIUS))
        assertEquals(32, 0.0.toDisplayTemp(TemperatureUnit.FAHRENHEIT))
        assertEquals(25, 25.0.toDisplayTemp(TemperatureUnit.CELSIUS))
        assertEquals(77, 25.0.toDisplayTemp(TemperatureUnit.FAHRENHEIT))
        assertEquals(212, 100.0.toDisplayTemp(TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun testGeocodingJsonParsing() {
        val json = """
            {
                "results": [
                    {
                        "id": 1642911,
                        "name": "Jakarta",
                        "latitude": -6.21462,
                        "longitude": 106.84513,
                        "country": "Indonesia",
                        "country_code": "ID",
                        "admin1": "Jakarta"
                    }
                ]
            }
        """.trimIndent()

        val response = gson.fromJson(json, GeocodingResponse::class.java)
        assertNotNull(response.results)
        assertEquals(1, response.results?.size)
        val item = response.results?.first()
        assertEquals("Jakarta", item?.name)
        assertEquals(-6.21462, item?.latitude ?: 0.0, 0.0001)
    }

    @Test
    fun testForecastJsonParsing() {
        val json = """
            {
                "latitude": -6.25,
                "longitude": 106.875,
                "timezone": "Asia/Jakarta",
                "current": {
                    "time": "2026-11-24T15:00",
                    "temperature_2m": 31.5,
                    "relative_humidity_2m": 75,
                    "apparent_temperature": 38.0,
                    "weather_code": 2,
                    "wind_speed_10m": 12.4
                },
                "daily": {
                    "time": ["2026-11-24", "2026-11-25"],
                    "weather_code": [2, 61],
                    "temperature_2m_max": [33.0, 31.5],
                    "temperature_2m_min": [24.0, 23.5]
                },
                "hourly": {
                    "time": ["2026-11-24T00:00", "2026-11-24T01:00"],
                    "temperature_2m": [25.0, 24.5],
                    "weather_code": [2, 3]
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ForecastResponse::class.java)
        assertNotNull(response.current)
        assertEquals(31.5, response.current?.temperature2m ?: 0.0, 0.01)
        assertEquals(75, response.current?.relativeHumidity2m)
        assertEquals(2, response.current?.weatherCode)

        assertNotNull(response.daily)
        assertEquals(2, response.daily?.time?.size)
        assertEquals(33.0, response.daily?.temperature2mMax?.first() ?: 0.0, 0.01)

        assertNotNull(response.hourly)
        assertEquals(2, response.hourly?.time?.size)
        assertEquals(25.0, response.hourly?.temperature2m?.first() ?: 0.0, 0.01)
    }
}
