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

    @Test
    fun testFavoriteCitiesAddDeleteAndReadd() {
        val city1 = CityLocation(1L, "Jakarta", "Indonesia", "DKI Jakarta", -6.2, 106.8)
        val city2 = CityLocation(2L, "Bandung", "Indonesia", "Jawa Barat", -6.9, 107.6)
        val city3 = CityLocation(3L, "Surabaya", "Indonesia", "Jawa Timur", -7.2, 112.7)

        // 1. Add 3 cities
        val list = mutableListOf(city1, city2, city3)
        assertEquals(3, list.size)

        // 2. Remove 1 city (Bandung, id = 2)
        val filteredAfterRemove1 = list.filter { city ->
            val matchId = city.id == 2L
            val matchName = city.name.equals("Bandung", ignoreCase = true)
            !(matchId || matchName)
        }.toMutableList()

        assertEquals(2, filteredAfterRemove1.size)
        assertTrue(filteredAfterRemove1.any { it.name == "Jakarta" })
        assertTrue(filteredAfterRemove1.any { it.name == "Surabaya" })
        assertTrue(filteredAfterRemove1.none { it.name == "Bandung" })

        // 3. Remove another city (Surabaya, id = 3)
        val filteredAfterRemove2 = filteredAfterRemove1.filter { city ->
            val matchId = city.id == 3L
            val matchName = city.name.equals("Surabaya", ignoreCase = true)
            !(matchId || matchName)
        }.toMutableList()

        assertEquals(1, filteredAfterRemove2.size)
        assertEquals("Jakarta", filteredAfterRemove2.first().name)

        // 4. Re-add Bandung
        val alreadyExists = filteredAfterRemove2.any { it.id == city2.id || it.name == city2.name }
        if (!alreadyExists) {
            filteredAfterRemove2.add(city2)
        }
        assertEquals(2, filteredAfterRemove2.size)
        assertTrue(filteredAfterRemove2.any { it.name == "Jakarta" })
        assertTrue(filteredAfterRemove2.any { it.name == "Bandung" })

        // 5. Test JSON roundtrip persistence
        val json = gson.toJson(filteredAfterRemove2)
        val type = object : com.google.gson.reflect.TypeToken<List<CityLocation>>() {}.type
        val restoredList: List<CityLocation> = gson.fromJson(json, type)
        assertEquals(2, restoredList.size)
        assertEquals("Jakarta", restoredList[0].name)
        assertEquals("Bandung", restoredList[1].name)
    }
}
