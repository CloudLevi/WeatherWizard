package com.cloudlevi.weatherwizard.data

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class LocationModel(val latitude: String, val longitude: String)

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore("user_preferences")

    val locationFlow: Flow<LocationModel> = dataStore.data
        .catch { exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }else {
                throw exception
            }
        }
        .map { preferences ->
            val latitude = preferences[PreferencesKeys.USER_LAST_LAT] ?: "0"
            val longitude = preferences[PreferencesKeys.USER_LAST_LON] ?: "0"

            LocationModel(latitude, longitude)
        }

    suspend fun setLastLocation(locationModel: LocationModel){
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_LAST_LAT] = locationModel.latitude
            preferences[PreferencesKeys.USER_LAST_LON] = locationModel.longitude
        }
    }

    suspend fun getLastLocation(): String {
        val lat = dataStore.data.first()[PreferencesKeys.USER_LAST_LAT] ?: ""
        val lon = dataStore.data.first()[PreferencesKeys.USER_LAST_LON] ?: ""

        return "Returned Latitude: $lat, Longitude: $lon}"
    }
}

private object PreferencesKeys {
    val USER_LAST_LAT = preferencesKey<String>("last_latitude")
    val USER_LAST_LON = preferencesKey<String>("last_longitude")
}