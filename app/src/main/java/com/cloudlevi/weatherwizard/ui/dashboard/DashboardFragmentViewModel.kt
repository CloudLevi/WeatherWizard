package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cloudlevi.weatherwizard.apiServiceWeatherByCoordinates
import com.cloudlevi.weatherwizard.data.*
import com.cloudlevi.weatherwizard.mainActivityAppID
import com.cloudlevi.weatherwizard.mainActivityUnits
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.FieldPosition
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardFragmentEvent.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class DashboardFragmentViewModel @ViewModelInject constructor(
    private val dataStoreManager: DataStoreManager,
    private val wizardDao: WizardDao
) : ViewModel() {

    var location = dataStoreManager.locationFlow.asLiveData()

    private lateinit var callWeatherByCoordinates: Call<WeatherLocationModel>
    private lateinit var callForecastByCoordinates: Call<WeatherForecastModel>

    var weatherLiveData = wizardDao.getAllWeather().asLiveData()

    var hourlyForecastLiveData = wizardDao.getHourlyForecast().asLiveData()
    var currentHourlyList = arrayListOf<HourlyConverted>()

    var dailyForecastLiveData = wizardDao.getDailyForecast().asLiveData()
    var currentDailyList = arrayListOf<DailyConverted>()

    var titleLiveData = MutableLiveData<String>()

    var updatingRecycler = false

    var listOfWeather = arrayListOf<WeatherConvertedModel>()

    private val dashboardFragmentEventChannel = Channel<DashboardFragmentEvent>()
    val dashboardFragmentEvent = dashboardFragmentEventChannel.receiveAsFlow()

    fun onLocationUpdated(locationModel: LocationModel) = viewModelScope.launch {

        callWeatherByCoordinates = apiServiceWeatherByCoordinates.getWeatherAtCoordinates(
            locationModel.latitude,
            locationModel.longitude,
            mainActivityAppID,
            mainActivityUnits
        )

        callWeatherByCoordinates.enqueue(object : Callback<WeatherLocationModel> {
            override fun onResponse(
                call: Call<WeatherLocationModel>,
                response: Response<WeatherLocationModel>
            ) {
                when (response.body()) {
                    null -> sendToastMessage("Error loading data")

                    is WeatherLocationModel -> {
                        val weatherModel = convertApiModel(response.body()!!)
                        weatherModel.id = 1
                        configureAndSetWeatherModel(weatherModel)
                    }
                }
            }

            override fun onFailure(call: Call<WeatherLocationModel>, t: Throwable) {
                sendToastMessage("Error loading data")
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })

    }

    fun getForecast(weatherModel: WeatherConvertedModel){

        callForecastByCoordinates = apiServiceWeatherByCoordinates.getWeatherForecast(
            weatherModel.lat.toString(),
            weatherModel.lon.toString(),
            "minutely",
            mainActivityAppID,
            mainActivityUnits
        )

        callForecastByCoordinates.enqueue(object : Callback<WeatherForecastModel> {
            override fun onResponse(
                call: Call<WeatherForecastModel>,
                response: Response<WeatherForecastModel>
            ) {
                when (response.body()) {
                    null -> sendToastMessage("Error loading data")

                    is WeatherForecastModel -> {
                        val weatherForecastModel = response.body()!!
                        val weatherConvertedForecast = convertApiForecast(weatherForecastModel, weatherModel.id)

                        saveForecast(weatherConvertedForecast)

                    }
                }
            }

            override fun onFailure(call: Call<WeatherForecastModel>, t: Throwable) {
                sendToastMessage("Error loading data")
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun updateWeatherModel(weatherConvertedModel: WeatherConvertedModel, refresh: Boolean = false) = viewModelScope.launch {

        if (refresh)
            getForecast(weatherConvertedModel)

        callWeatherByCoordinates = apiServiceWeatherByCoordinates.getWeatherAtCoordinates(
            weatherConvertedModel.lat.toString(),
            weatherConvertedModel.lon.toString(),
            mainActivityAppID,
            mainActivityUnits
        )

        callWeatherByCoordinates.enqueue(object : Callback<WeatherLocationModel> {
            override fun onResponse(
                call: Call<WeatherLocationModel>,
                response: Response<WeatherLocationModel>
            ) {
                stopRefreshLoader()

                when (response.body()) {
                    null -> sendToastMessage("Error loading data")

                    is WeatherLocationModel -> {
                        val weatherModel = convertApiModel(response.body()!!)
                        weatherModel.id = weatherConvertedModel.id
                        configureAndSetWeatherModel(weatherModel)
                    }
                }
            }

            override fun onFailure(call: Call<WeatherLocationModel>, t: Throwable) {
                stopRefreshLoader()
                sendToastMessage("Error loading data")
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun addObservedLocation(cityEntryModel: CityEntryModel) = viewModelScope.launch {
        callWeatherByCoordinates = apiServiceWeatherByCoordinates.getWeatherAtCoordinates(
            (cityEntryModel.lat?:0.0).toString(),
            (cityEntryModel.lng?:0.0).toString(),
            mainActivityAppID,
            mainActivityUnits
        )

        callWeatherByCoordinates.enqueue(object : Callback<WeatherLocationModel> {
            override fun onResponse(
                call: Call<WeatherLocationModel>,
                response: Response<WeatherLocationModel>
            ) {
                when (response.body()) {
                    null -> sendToastMessage("Error loading data")

                    is WeatherLocationModel -> {
                        val weatherModel = convertApiModel(response.body()!!)
                        configureAndSetWeatherModel(weatherModel)
                    }
                }
            }

            override fun onFailure(call: Call<WeatherLocationModel>, t: Throwable) {
                sendToastMessage("Error loading data")
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun getFragmentHourlyForecast(position: Int): ArrayList<HourlyConverted>{
        val fragmentHourlyArray = arrayListOf<HourlyConverted>()
        for (item in currentHourlyList){
            if (item.modelID == listOfWeather[position].id)
                fragmentHourlyArray.add(item)
        }
        return fragmentHourlyArray
    }

    fun getFragmentDailyForecast(position: Int): ArrayList<DailyConverted>{
        val fragmentDailyArray = arrayListOf<DailyConverted>()

        for (item in currentDailyList){
            if (item.modelID == listOfWeather[position].id)
                fragmentDailyArray.add(item)
        }
        return fragmentDailyArray
    }

    fun getDashboardInfoList(weatherModel: WeatherConvertedModel): ArrayList<WeatherDashboardModel>{
        val arrayDashboardInfoModelList = arrayListOf<WeatherDashboardModel>()

        arrayDashboardInfoModelList.apply {
            add(WeatherDashboardModel("SUNRISE", getCurrentTime(weatherModel.sunrise, weatherModel.timezone)))
            add(WeatherDashboardModel("SUNSET", getCurrentTime(weatherModel.sunset, weatherModel.timezone)))
            add(WeatherDashboardModel("FEELS LIKE", "${weatherModel.feels_like}°C"))
            add(WeatherDashboardModel("SEA LEVEL", "${weatherModel.sea_level} m"))
            add(WeatherDashboardModel("HUMIDITY", "${weatherModel.humidity}%"))
            add(WeatherDashboardModel("WIND SPEED", "${weatherModel.speed} km/h"))
            add(WeatherDashboardModel("PRESSURE", "${weatherModel.pressure} hPa"))
            add(WeatherDashboardModel("CLOUDINESS", "${weatherModel.cloudiness}%"))
        }

        return arrayDashboardInfoModelList
    }

    private fun saveForecast(weatherConvertedForecast: WeatherConvertedForecast) = viewModelScope.launch {

        wizardDao.deleteItemHourlyForecast(weatherConvertedForecast.id)
        wizardDao.deleteItemDailyForecast(weatherConvertedForecast.id)

        wizardDao.insertHourlyList(weatherConvertedForecast.hourlyList)
        wizardDao.insertDailyList(weatherConvertedForecast.dailyList)
    }

    private fun configureAndSetWeatherModel(weatherModel: WeatherConvertedModel) {
        if (weatherModel.name == "") weatherModel.name = "Unnamed location"
        weatherModel.temp = floor(weatherModel.temp)
        weatherModel.speed = floor(weatherModel.speed * 3.6)

        weatherModel.currentTime = getCurrentTime(weatherModel)

        saveWeatherModel(weatherModel)
    }

    private fun saveWeatherModel(weatherModel: WeatherConvertedModel) = viewModelScope.launch {
        stopRefreshLoader()
        wizardDao.insertWeatherModel(weatherModel)
        notifyWeatherModelSaved(weatherModel)
    }

    private fun notifyWeatherModelSaved(weatherModel: WeatherConvertedModel) = viewModelScope.launch {
        dashboardFragmentEventChannel.send(NotifyWeatherModelSaved(weatherModel))
    }

    fun timeUpdated(){
        for (currentWeather in listOfWeather){
            currentWeather.currentTime = getCurrentTime(currentWeather)
            saveWeatherModel(currentWeather)
        }
    }

    private fun getCurrentTime(weatherModel: WeatherConvertedModel): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(Date().time.plus(weatherModel.timezone * 1000))
    }

    private fun getCurrentTime(time: Long, timezone: Int): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(time.plus(timezone * 1000))
    }

    private fun getCurrentDay(time: Long, timezone: Int): String {
        val simpleDateFormat = SimpleDateFormat("EEEE")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(time.plus(timezone * 1000))
    }

    private fun stopRefreshLoader() = viewModelScope.launch {
        dashboardFragmentEventChannel.send(StopRefreshLoader)
    }

    private fun sendToastMessage(message: String) = viewModelScope.launch {
        dashboardFragmentEventChannel.send(SendToastMessage(message))
    }

    fun convertApiModel(apiWeatherModel: WeatherLocationModel): WeatherConvertedModel {
        apiWeatherModel.apply {
            return WeatherConvertedModel(
                name = name,
                currentTime = currentTime,
                lat = coord.lat,
                lon = coord.lon,
                temp = main.temp,
                dt = dt * 1000,
                main = weather[0].main,
                feels_like = main.feels_like,
                humidity = main.humidity,
                sea_level = main.sea_level,
                country = sys.country,
                cloudiness = clouds.all,
                pressure = main.pressure,
                //Multiply by 1000 to convert from seconds to milliseconds
                sunrise = sys.sunrise * 1000,
                sunset = sys.sunset * 1000,
                description = weather[0].description,
                speed = wind.speed,
                timezone = timezone
            )
        }
    }

    fun convertApiForecast(apiWeatherForecast: WeatherForecastModel, id: Int): WeatherConvertedForecast {
        val hourlyList = arrayListOf<HourlyConverted>()
        val dailyList = arrayListOf<DailyConverted>()

        var hourlyCount = 0

        //Convert hourly list
        for (item in apiWeatherForecast.hourly){
            if (hourlyCount < 25){
                hourlyList.add(
                    HourlyConverted(
                        modelID = id,
                        hourlyDt = item.dt * 1000,
                        hourlyTime = getCurrentTime(item.dt * 1000, apiWeatherForecast.timezone_offset),
                        hourlyTemp = item.temp,
                        hourlyPrecipitation = (item.pop * 100).toInt(),
                        hourlyIcon = item.weather[0].icon
                    )
                )
                hourlyCount++
            }
        }

        //Convert daily list
        for (item in apiWeatherForecast.daily){
            dailyList.add(
                DailyConverted(
                    modelID = id,
                    dailyDt = item.dt * 1000,
                    dailyText = getCurrentDay(item.dt * 1000, apiWeatherForecast.timezone_offset),
                    dailyTempMin = item.temp.min,
                    dailyTempMax = item.temp.max,
                    dailyPrecipitation = (item.pop * 100).toInt(),
                    dailyIcon = item.weather[0].icon
                )
            )
        }

        return WeatherConvertedForecast(
            currentDt = apiWeatherForecast.current.dt * 1000,
            currentTime = getCurrentTime(apiWeatherForecast.current.dt * 1000, apiWeatherForecast.timezone_offset),
            currentTemp = apiWeatherForecast.current.temp,
            currentIcon = apiWeatherForecast.current.weather[0].icon,
            hourlyList = hourlyList,
            dailyList = dailyList,
            lat = apiWeatherForecast.lat,
            lon = apiWeatherForecast.lon,
            timezone = apiWeatherForecast.timezone_offset,
            id = id
        )
    }
}

sealed class DashboardFragmentEvent {
    data class NotifyWeatherModelSaved(val weatherModel: WeatherConvertedModel) : DashboardFragmentEvent()
    data class SendToastMessage(val message: String): DashboardFragmentEvent()
    object StopRefreshLoader: DashboardFragmentEvent()
}