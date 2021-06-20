package com.cloudlevi.weatherwizard

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cloudlevi.weatherwizard.data.DataStoreManager
import com.cloudlevi.weatherwizard.data.LocationModel
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.data.WizardDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel @ViewModelInject constructor(
    application: Application,
    private val wizardDao: WizardDao,
    private val dataStoreManager: DataStoreManager
): AndroidViewModel(application) {

    val weatherListLiveData = wizardDao.getAllWeather().asLiveData()

    private var timeChangedActive = false

    var currentWeatherList = arrayListOf<WeatherConvertedModel>()

    fun saveLocation(locationModel: LocationModel) = viewModelScope.launch {
        dataStoreManager.setLastLocation(locationModel)
    }

    fun onTimeChanged(){
        if (!timeChangedActive){
            timeChangedActive = true
            for ((index, weather) in currentWeatherList.withIndex()){
                currentWeatherList[index] = weather.copy(currentTime = getCurrentTime(weather))
            }
            insertWeatherList(currentWeatherList)
            timeChangedActive = false
        }
    }

    private fun insertWeather(weatherConvertedModel: WeatherConvertedModel) = viewModelScope.launch {
        wizardDao.insertWeatherModel(weatherConvertedModel)
    }

    private fun insertWeatherList(weatherList: List<WeatherConvertedModel>) = viewModelScope.launch {
        wizardDao.insertWeatherList(weatherList)
    }

    private fun getCurrentTime(weatherModel: WeatherConvertedModel): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(Date().time.plus(weatherModel.timezone * 1000))
    }
}