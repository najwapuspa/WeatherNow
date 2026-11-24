package com.example.weathernow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.R
import com.example.weathernow.data.model.CurrentWeather
import com.example.weathernow.data.model.TemperatureUnit
import com.example.weathernow.data.model.toDisplayTemp
import kotlin.math.roundToInt

@Composable
fun WeatherMetricsRow(
    current: CurrentWeather,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val displayFeelsLike = current.feelsLike.toDisplayTemp(temperatureUnit)

    val humidityTitle = stringResource(R.string.humidity_title)
    val humiditySubtitle = if (current.humidity > 70) {
        stringResource(R.string.humidity_high)
    } else {
        stringResource(R.string.humidity_normal)
    }

    val windTitle = stringResource(R.string.wind_speed_title)
    val windSubtitle = if (current.windSpeed > 20) {
        stringResource(R.string.wind_strong)
    } else {
        stringResource(R.string.wind_calm)
    }

    val feelsLikeTitle = stringResource(R.string.feels_like_title)
    val feelsLikeSubtitle = stringResource(R.string.feels_like_subtitle)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Humidity
        WeatherMetricCard(
            title = humidityTitle,
            value = "${current.humidity}%",
            subtitle = humiditySubtitle,
            icon = Icons.Default.WaterDrop,
            accentColor = Color(0xFF247BA0),
            modifier = Modifier.weight(1f)
        )

        // Wind Speed
        WeatherMetricCard(
            title = windTitle,
            value = "${current.windSpeed.roundToInt()} km/j",
            subtitle = windSubtitle,
            icon = Icons.Default.Air,
            accentColor = Color(0xFF00A8E8),
            modifier = Modifier.weight(1f)
        )

        // Feels Like
        WeatherMetricCard(
            title = feelsLikeTitle,
            value = "$displayFeelsLike${temperatureUnit.symbol}",
            subtitle = feelsLikeSubtitle,
            icon = Icons.Default.Thermostat,
            accentColor = Color(0xFFE65100),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WeatherMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
