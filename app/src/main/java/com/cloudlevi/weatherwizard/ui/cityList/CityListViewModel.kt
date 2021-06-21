package com.cloudlevi.weatherwizard.ui.cityList

import android.content.ContentValues.TAG
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.data.WizardDao
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.FieldPosition

class CityListViewModel @ViewModelInject constructor(
    private val wizardDao: WizardDao
): ViewModel() {

    val weatherListLiveData = wizardDao.getAllWeather().asLiveData()

    var searchQuery = MutableLiveData<String>()

    var deletedPosition: Int = -1

    var queryCitiesFlow = searchQuery.asFlow().flatMapLatest {
        wizardDao.getCitiesByQuery(it, 0)
    }

    var currentItemSwiped = -1

    val queryCitiesLiveData = queryCitiesFlow.asLiveData()

    fun onQueryChanged(query: String, cancel: Boolean = false){
        if (!cancel) searchQuery.value = query
    }

    fun onDeleteClicked(position: Int) = viewModelScope.launch {
        val weather = weatherListLiveData.value?.get(position)
        if (weather != null) {
            wizardDao.deleteObservedWeather(weather)
            deletedPosition = position
        }
        else Log.d(TAG, "onDeleteClicked: Weather Model is null at index: $position")
    }
}