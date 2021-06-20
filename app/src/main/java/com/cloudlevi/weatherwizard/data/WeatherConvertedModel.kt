package com.cloudlevi.weatherwizard.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "weather_table")
@Parcelize
data class WeatherConvertedModel constructor(
    var name: String = "",
    var currentTime: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var temp: Double = 0.0,
    var feels_like: Double = 0.0,
    var main: String = "",
    var humidity: Int = 0,
    var dt: Long = 0,
    var sea_level: Int = 0,
    var pressure: Int = 0,
    var cloudiness: Int = 0,
    var country: String = "",
    var sunrise: Long = 0,
    var sunset: Long = 0,
    var description: String = "",
    var speed: Double = 0.0,
    var timezone: Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
): Parcelable {
}