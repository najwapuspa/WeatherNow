package com.example.weathernow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.data.local.PreferencesManager
import com.example.weathernow.data.model.CityLocation
import com.example.weathernow.data.model.FavoriteCityWeather
import com.example.weathernow.data.model.TemperatureUnit
import com.example.weathernow.data.model.ThemeMode
import com.example.weathernow.data.model.WeatherDashboardData
import com.example.weathernow.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface WeatherUiState {
    data class Loading(val message: String = "Memuat data cuaca...") : WeatherUiState
    data class Success(val data: WeatherDashboardData) : WeatherUiState
    data class Error(val message: String, val canRetry: Boolean = true) : WeatherUiState
}

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<CityLocation> = emptyList(),
    val searchError: String? = null,
    val isDropdownOpen: Boolean = false
)

class WeatherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: WeatherRepository = WeatherRepository(),
    private val preferencesManager: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    // --- Theme & Unit Preferences State ---
    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val temperatureUnit: StateFlow<TemperatureUnit> = preferencesManager.temperatureUnitFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemperatureUnit.CELSIUS
    )

    // --- Weather Dashboard UI State ---
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading("Memuat cuaca terkini..."))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // --- Selected Day index in Forecast Screen ---
    private val _selectedForecastDayIndex = MutableStateFlow(0)
    val selectedForecastDayIndex: StateFlow<Int> = _selectedForecastDayIndex.asStateFlow()

    // --- Favorite Cities Weather State ---
    private val _favoriteCitiesWeather = MutableStateFlow<List<FavoriteCityWeather>>(emptyList())
    val favoriteCitiesWeather: StateFlow<List<FavoriteCityWeather>> = _favoriteCitiesWeather.asStateFlow()

    private var currentCity: CityLocation = WeatherRepository.DEFAULT_CITY
    private var favoriteCitiesList: List<CityLocation> = emptyList()

    init {
        // Observe favorite cities from DataStore
        viewModelScope.launch {
            preferencesManager.favoriteCitiesFlow.collect { cities ->
                favoriteCitiesList = cities
                updateFavoriteWeatherList(cities)
                // If current data is Success, sync its isFavorite flag
                val current = _uiState.value
                if (current is WeatherUiState.Success) {
                    val isFav = cities.any { it.id == current.data.city.id }
                    if (current.data.isFavorite != isFav) {
                        _uiState.value = WeatherUiState.Success(current.data.copy(isFavorite = isFav))
                    }
                }
            }
        }

        loadWeather(currentCity)
    }

    fun loadWeather(city: CityLocation) {
        currentCity = city
        _selectedForecastDayIndex.value = 0
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading("Mengambil prakiraan cuaca untuk ${city.name}...")
            val isFav = favoriteCitiesList.any { it.id == city.id }
            val result = repository.getWeather(city, isFavorite = isFav)
            result.fold(
                onSuccess = { data ->
                    _uiState.value = WeatherUiState.Success(data)
                },
                onFailure = { error ->
                    _uiState.value = WeatherUiState.Error(
                        message = parseErrorMessage(error)
                    )
                }
            )
        }
    }

    fun refreshWeather() {
        loadWeather(currentCity)
        updateFavoriteWeatherList(favoriteCitiesList)
    }

    fun selectForecastDay(index: Int) {
        _selectedForecastDayIndex.value = index
    }

    // --- Search Logic ---
    fun onSearchQueryChanged(newQuery: String) {
        _searchState.value = _searchState.value.copy(
            query = newQuery,
            searchError = null,
            isDropdownOpen = newQuery.isNotBlank() && _searchState.value.results.isNotEmpty()
        )
    }

    fun searchCity() {
        val query = _searchState.value.query.trim()
        if (query.isEmpty()) {
            _searchState.value = _searchState.value.copy(
                searchError = "Silakan masukkan nama kota terlebih dahulu."
            )
            return
        }

        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                isSearching = true,
                searchError = null
            )

            val result = repository.searchCities(query)
            result.fold(
                onSuccess = { cities ->
                    if (cities.isEmpty()) {
                        _searchState.value = _searchState.value.copy(
                            isSearching = false,
                            results = emptyList(),
                            searchError = "Kota \"$query\" tidak ditemukan. Coba nama kota lain.",
                            isDropdownOpen = false
                        )
                    } else {
                        _searchState.value = _searchState.value.copy(
                            isSearching = false,
                            results = cities,
                            searchError = null,
                            isDropdownOpen = true
                        )
                    }
                },
                onFailure = { error ->
                    _searchState.value = _searchState.value.copy(
                        isSearching = false,
                        results = emptyList(),
                        searchError = parseErrorMessage(error),
                        isDropdownOpen = false
                    )
                }
            )
        }
    }

    fun selectCity(city: CityLocation) {
        _searchState.value = SearchUiState(
            query = "",
            isSearching = false,
            results = emptyList(),
            searchError = null,
            isDropdownOpen = false
        )
        loadWeather(city)
    }

    fun dismissSearchDropdown() {
        _searchState.value = _searchState.value.copy(isDropdownOpen = false)
    }

    // --- Favorites Logic ---
    fun toggleFavorite(city: CityLocation) {
        viewModelScope.launch {
            val isFav = favoriteCitiesList.any { it.id == city.id }
            if (isFav) {
                preferencesManager.removeFavoriteCity(city.id)
            } else {
                preferencesManager.addFavoriteCity(city)
            }
        }
    }

    fun removeFavorite(cityId: Long) {
        viewModelScope.launch {
            preferencesManager.removeFavoriteCity(cityId)
        }
    }

    private fun updateFavoriteWeatherList(cities: List<CityLocation>) {
        viewModelScope.launch {
            val items = cities.map { city ->
                FavoriteCityWeather(
                    city = city,
                    isLoading = true
                )
            }
            _favoriteCitiesWeather.value = items

            val updatedItems = cities.map { city ->
                val weatherResult = repository.getWeather(city)
                weatherResult.fold(
                    onSuccess = { data ->
                        FavoriteCityWeather(
                            city = city,
                            currentTemp = data.current.temperature,
                            weatherCode = data.current.weatherCode,
                            conditionDescription = data.current.conditionDescription,
                            isLoading = false
                        )
                    },
                    onFailure = {
                        FavoriteCityWeather(
                            city = city,
                            isLoading = false
                        )
                    }
                )
            }
            _favoriteCitiesWeather.value = updatedItems
        }
    }

    // --- Settings Logic ---
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            preferencesManager.setTemperatureUnit(unit)
        }
    }

    private fun parseErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "Koneksi internet terputus. Pastikan perangkat Anda terhubung ke internet."
            is SocketTimeoutException -> "Koneksi ke server timeout. Silakan coba kembali."
            is IOException -> "Terjadi masalah jaringan: Tidak dapat menghubungi server Open-Meteo."
            is HttpException -> "Server mengembalikan respons kesalahan (${throwable.code()}). Silakan coba sesaat lagi."
            else -> throwable.localizedMessage ?: "Terjadi kesalahan yang tidak terduga. Silakan coba lagi."
        }
    }
}
