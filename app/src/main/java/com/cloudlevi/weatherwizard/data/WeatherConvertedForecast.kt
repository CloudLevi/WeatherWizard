package com.cloudlevi.weatherwizard.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

data class WeatherConvertedForecast(
    var currentDt: Long = 0,
    var currentTime: String = "",
    var currentTemp: Double = 0.0,
    var currentIcon: String = "",
    var hourlyList: List<HourlyConverted>,
    var dailyList: List<DailyConverted>,
    var lat: Double,
    var lon: Double,
    var timezone: Int,
    var id: Int = 0
)

@Entity(tableName = "hourly_forecast")
data class HourlyConverted(
    var modelID: Int = 0,
    var hourlyDt: Long = 0,
    var hourlyTime: String = "",
    var hourlyTemp: Double = 0.0,
    var hourlyIcon: String = "",
    var hourlyPrecipitation: Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)

@Entity(tableName = "daily_forecast")
data class DailyConverted(
    var modelID: Int = 0,
    var dailyDt: Long = 0,
    var dailyText: String = "",
    var dailyTempMin: Double = 0.0,
    var dailyTempMax: Double = 0.0,
    var dailyPrecipitation: Int = 0,
    var dailyIcon: String = "",
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)