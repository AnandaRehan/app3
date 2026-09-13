package com.ehan.app3

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ehan.app3.data.UserPreferencesRepository

// Ekstensi DataStore Preferences pada Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Custom Application class yang didaftarkan di AndroidManifest.xml:
 * <application
 *     android:name=".Application"
 *     ... >
 */
class Kalkulator : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }

    companion object {
        lateinit var instance: Kalkulator
            private set
    }
}