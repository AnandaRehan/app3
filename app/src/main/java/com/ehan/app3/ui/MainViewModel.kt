package com.ehan.app3.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.isSystemInDarkTheme
import com.ehan.app3.App3
import com.ehan.app3.data.UserPreferences
import com.ehan.app3.data.UserPreferencesRepository
import com.ehan.app3.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String
)

class MainViewModel(
    application: Application,
    private val repository: UserPreferencesRepository = (application as App3).userPreferencesRepository
) : AndroidViewModel(application) {

    // Mengambil preferences secara reaktif dari DataStore melalui Flow
    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _statusMessage = MutableStateFlow<UiNotification?>(null)
    val statusMessage: StateFlow<UiNotification?> = _statusMessage.asStateFlow()

    val darkTheme: Boolean = when (userPreferences.themeMode) {
        ThemeMode.DARK.label -> {
            true
        }
        ThemeMode.LIGHT.label -> {
            false
        }
        else -> {
            isSystemInDarkTheme ?: false
        }
    }

    fun setUsername(newUsername: String) {
        val trimmed = newUsername.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                repository.updateUsername(trimmed)
                _statusMessage.value = UiNotification(message = "Nama berhasil disimpan ke DataStore!")
            }
        }
    }

    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            repository.setThemeMode(theme)
            _statusMessage.value = UiNotification(message = "Theme Mode Diubah ke $theme!")
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            _statusMessage.value = UiNotification(
                message = if (enabled) "Notifikasi diaktifkan" else "Notifikasi dimatikan"
            )
        }
    }

    fun incrementCounter() {
        viewModelScope.launch {
            repository.incrementCounter()
        }
    }

    fun selectAccentColor(color: String) {
        viewModelScope.launch {
            repository.setAccentColor(color)
            _statusMessage.value = UiNotification(message = "Warna aksen diubah ke $color")
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearAll()
            _statusMessage.value = UiNotification(message = "DataStore Preferences telah di-reset ke nilai awal")
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application) as T
                }
            }
    }
}