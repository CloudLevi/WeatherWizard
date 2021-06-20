package com.cloudlevi.weatherwizard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WizardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherModel(weatherModel: WeatherConvertedModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherList(weatherList: List<WeatherConvertedModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyList(hourlyList: List<HourlyConverted>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyList(dailyList: List<DailyConverted>)

    @Query("SELECT * FROM hourly_forecast")
    fun getHourlyForecast(): Flow<List<HourlyConverted>>

    @Query("SELECT * FROM daily_forecast")
    fun getDailyForecast(): Flow<List<DailyConverted>>

    @Query("SELECT * FROM weather_table")
    fun getAllWeather(): Flow<List<WeatherConvertedModel>>

    @Delete
    suspend fun deleteObservedWeather(weatherConvertedModel: WeatherConvertedModel)

    @Query("DELETE FROM hourly_forecast WHERE modelID = :modelID")
    suspend fun deleteItemHourlyForecast(modelID: Int)

    @Query("DELETE FROM daily_forecast WHERE modelID = :modelID")
    suspend fun deleteItemDailyForecast(modelID: Int)

    @Query("SELECT * FROM world_cities")
    fun getAllCities(): Flow<List<CityEntryModel>>

    @Query("SELECT * FROM world_cities WHERE (city_ascii LIKE '%' || :searchQuery || '%' OR country LIKE '%' || :searchQuery || '%') LIMIT 100 OFFSET :offset")
    fun getCitiesByQuery(searchQuery: String, offset: Int): Flow<List<CityEntryModel>>

}