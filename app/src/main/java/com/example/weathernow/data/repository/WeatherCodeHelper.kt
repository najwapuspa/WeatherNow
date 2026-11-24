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

object WeatherCodeHelper {

    fun getDescription(code: Int): String {
        return when (code) {
            0 -> "Cerah (Clear Sky)"
            1 -> "Cerah Berawan (Mainly Clear)"
            2 -> "Berawan Sebagian (Partly Cloudy)"
            3 -> "Mendung (Overcast)"
            45, 48 -> "Berkabut (Foggy)"
            51 -> "Gerimis Ringan (Light Drizzle)"
            53 -> "Gerimis Sedang (Moderate Drizzle)"
            55 -> "Gerimis Lebat (Dense Drizzle)"
            56, 57 -> "Gerimis Beku (Freezing Drizzle)"
            61 -> "Hujan Ringan (Slight Rain)"
            63 -> "Hujan Sedang (Moderate Rain)"
            65 -> "Hujan Lebat (Heavy Rain)"
            66, 67 -> "Hujan Beku (Freezing Rain)"
            71 -> "Salju Ringan (Slight Snow)"
            73 -> "Salju Sedang (Moderate Snow)"
            75 -> "Salju Lebat (Heavy Snow)"
            77 -> "Hujan Butir Es (Snow Grains)"
            80 -> "Hujan Guyur Ringan (Light Showers)"
            81 -> "Hujan Guyur Sedang (Moderate Showers)"
            82 -> "Hujan Guyur Lebat (Violent Showers)"
            85, 86 -> "Hujan Salju Guyur (Snow Showers)"
            95 -> "Badai Petir (Thunderstorm)"
            96, 99 -> "Badai Petir & Hujan Es (Severe Thunderstorm)"
            else -> "Cuaca Bervariasi"
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
            // Cerah / Sunny (Blue to cyan/soft gold)
            0, 1 -> listOf(
                Color(0xFF1E88E5), // Bright vibrant blue
                Color(0xFF1565C0), // Deep blue
                Color(0xFF0D47A1)  // Rich navy
            )
            // Berawan / Cloudy (Soft slate blue to navy)
            2, 3, 45, 48 -> listOf(
                Color(0xFF37474F), // Slate blue grey
                Color(0xFF263238), // Dark slate
                Color(0xFF1A237E)  // Navy
            )
            // Hujan / Rain (Teal blue to dark ocean)
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(
                Color(0xFF00695C), // Deep teal
                Color(0xFF004D40), // Darker teal
                Color(0xFF0A192F)  // Dark navy
            )
            // Badai Petir / Thunderstorm (Deep indigo to dark midnight navy)
            95, 96, 99 -> listOf(
                Color(0xFF311B92), // Deep purple-navy
                Color(0xFF1A237E), // Deep navy
                Color(0xFF0D1B2A)  // Midnight blue
            )
            // Salju / Beku / Default
            else -> listOf(
                Color(0xFF1976D2),
                Color(0xFF0D47A1),
                Color(0xFF0A192F)
            )
        }
    }
}
