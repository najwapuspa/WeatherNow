package com.example.weathernow

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weathernow.data.model.AppLanguage
import com.example.weathernow.ui.navigation.Screen
import com.example.weathernow.ui.navigation.WeatherBottomBar
import com.example.weathernow.ui.screens.FavoritesScreen
import com.example.weathernow.ui.screens.ForecastScreen
import com.example.weathernow.ui.screens.HomeScreen
import com.example.weathernow.ui.screens.SettingsScreen
import com.example.weathernow.ui.theme.WeatherNowTheme
import com.example.weathernow.viewmodel.WeatherViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val weatherViewModel: WeatherViewModel = viewModel()
            val themeMode by weatherViewModel.themeMode.collectAsStateWithLifecycle()
            val appLanguage by weatherViewModel.appLanguage.collectAsStateWithLifecycle()

            val currentContext = LocalContext.current
            val currentConfig = LocalConfiguration.current

            val locale = if (appLanguage == AppLanguage.INDONESIAN) {
                Locale.forLanguageTag("id-ID")
            } else {
                Locale.ENGLISH
            }

            val localizedConfiguration = remember(locale, currentConfig) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }

            val localizedContext = remember(locale, currentContext) {
                val config = Configuration(currentContext.resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                currentContext.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                WeatherNowTheme(themeMode = themeMode) {
                    MainApp(viewModel = weatherViewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: WeatherViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            WeatherBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Forecast.route) {
                ForecastScreen(viewModel = viewModel)
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
