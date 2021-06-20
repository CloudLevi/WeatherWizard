package com.cloudlevi.weatherwizard.api

import com.cloudlevi.weatherwizard.data.WeatherForecastModel
import com.cloudlevi.weatherwizard.data.WeatherLocationModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("weather")
    fun getWeatherAtCoordinates(
        @Query("lat") userLat: String,
        @Query("lon") userLon: String,
        @Query("appid") appID: String,
        @Query("units") units: String
    ): Call<WeatherLocationModel>

    @GET("onecall")
    fun getWeatherForecast(
        @Query("lat") userLat: String,
        @Query("lon") userLon: String,
        @Query("exclude") exclude: String,
        @Query("appid") appID: String,
        @Query("units") units: String
    ): Call<WeatherForecastModel>
}