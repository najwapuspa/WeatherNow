package com.example.weathernow.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weathernow.data.model.AppLanguage

object WeatherCodeHelper {

    fun getDescription(code: Int, language: AppLanguage = AppLanguage.INDONESIAN): String {
        return when (language) {
            AppLanguage.INDONESIAN -> getIndonesianDescription(code)
            AppLanguage.ENGLISH -> getEnglishDescription(code)
        }
    }

    private fun getIndonesianDescription(code: Int): String {
        return when (code) {
            0 -> "Cerah"
            1 -> "Cerah Berawan"
            2 -> "Berawan Sebagian"
            3 -> "Mendung"
            45, 48 -> "Berkabut"
            51 -> "Gerimis Ringan"
            53 -> "Gerimis Sedang"
            55 -> "Gerimis Lebat"
            56, 57 -> "Gerimis Beku"
            61 -> "Hujan Ringan"
            63 -> "Hujan Sedang"
            65 -> "Hujan Lebat"
            66, 67 -> "Hujan Beku"
            71 -> "Salju Ringan"
            73 -> "Salju Sedang"
            75 -> "Salju Lebat"
            77 -> "Hujan Butir Es"
            80 -> "Hujan Guyur Ringan"
            81 -> "Hujan Guyur Sedang"
            82 -> "Hujan Guyur Lebat"
            85, 86 -> "Hujan Salju Guyur"
            95 -> "Badai Petir"
            96, 99 -> "Badai Petir & Hujan Es"
            else -> "Cuaca Bervariasi"
        }
    }

    private fun getEnglishDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71 -> "Slight Snow"
            73 -> "Moderate Snow"
            75 -> "Heavy Snow"
            77 -> "Snow Grains"
            80 -> "Light Showers"
            81 -> "Moderate Showers"
            82 -> "Violent Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Severe Thunderstorm"
            else -> "Variable Weather"
        }
    }

    fun getIcon(code: Int): ImageVector {
        return when (code) {
            0 -> Icons.Filled.WbSunny
            1 -> Icons.Filled.WbCloudy
            2 -> Icons.Filled.CloudQueue
            3 -> Icons.Filled.Cloud
            45, 48 -> Icons.Filled.CloudQueue
            51, 53, 55 -> Icons.Filled.Grain
            56, 57 -> Icons.Filled.AcUnit
            61, 63 -> Icons.Filled.WaterDrop
            65 -> Icons.Filled.WaterDrop
            66, 67 -> Icons.Filled.AcUnit
            71, 73, 75, 77 -> Icons.Filled.AcUnit
            80, 81, 82 -> Icons.Filled.WaterDrop
            85, 86 -> Icons.Filled.AcUnit
            95, 96, 99 -> Icons.Filled.Thunderstorm
            else -> Icons.Filled.Cloud
        }
    }

    fun getGradientColors(code: Int): List<Color> {
        return when (code) {
            0, 1 -> listOf(
                Color(0xFF1E88E5),
                Color(0xFF1565C0),
                Color(0xFF0D47A1)
            )
            2, 3, 45, 48 -> listOf(
                Color(0xFF37474F),
                Color(0xFF263238),
                Color(0xFF1A237E)
            )
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(
                Color(0xFF00695C),
                Color(0xFF004D40),
                Color(0xFF0A192F)
            )
            95, 96, 99 -> listOf(
                Color(0xFF311B92),
                Color(0xFF1A237E),
                Color(0xFF0D1B2A)
            )
            else -> listOf(
                Color(0xFF1976D2),
                Color(0xFF0D47A1),
                Color(0xFF0A192F)
            )
        }
    }
}
