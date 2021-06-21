package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.ContentValues.TAG
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.ui.cityList.CityListFragment
import com.cloudlevi.weatherwizard.ui.cityList.CityListObservedAdapter

class DashboardViewPagerAdapter(
    private val fragmentHost: Fragment,
    private var weatherList: List<WeatherConvertedModel>,
) : FragmentStateAdapter(fragmentHost) {
    private val listOfFragments = HashMap<Int, DashboardFragmentItem>()
    private var arrayWeatherList = ArrayList(weatherList)
        set(value) {
            field = value
            weatherList = value
        }

    private var firstLaunch = false

    override fun getItemCount(): Int {
        return arrayWeatherList.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        return if (position != arrayWeatherList.size || firstLaunch) {
            val fragment = DashboardFragmentItem
                .getInstance(position, arrayWeatherList[position], (fragmentHost as DashboardHost))
            listOfFragments[position] = (fragment as DashboardFragmentItem)

            fragment
        } else {
            CityListFragment.getInstance((fragmentHost as DashboardHost))
        }
    }

    fun submitList(newWeatherList: List<WeatherConvertedModel>) {

        if (arrayWeatherList.size != newWeatherList.size) {
            when {
                arrayWeatherList.size - newWeatherList.size == -1 -> {
                    arrayWeatherList = ArrayList(newWeatherList)
                    notifyItemInserted(arrayWeatherList.size - 1)
                }
                else -> (fragmentHost as DashboardHost).invalidateAdapter(ArrayList(newWeatherList), arrayWeatherList.size)
            }
        } else {
            if (arrayWeatherList.size != 0) {
                //Compare old items with new, update UI
                for ((index, weatherOld) in arrayWeatherList.withIndex()) {
                    val weather = newWeatherList[index]
                    if (weather != arrayWeatherList[index]) {
                        arrayWeatherList[index] = weather

                        try {
                            (listOfFragments[index] as DashboardFragmentItem).locationChanged(weather)
                        }
                        catch (e: NullPointerException){
                            Log.d(TAG, "Fragment listener not initialized yet at: ${index}")
                        }
                    }
                    else{
                        try {
                            (listOfFragments[index] as DashboardFragmentItem).stopRefreshLoader()
                        }
                        catch (e: NullPointerException){
                            Log.d(TAG, "Fragment listener not initialized yet at: ${index}")
                        }
                    }
                }

            } else {
                arrayWeatherList = ArrayList(newWeatherList)
                (fragmentHost as DashboardHost).invalidateAdapter(arrayWeatherList)
            }
        }
    }

    private fun findDeletedItem(biggerList: ArrayList<WeatherConvertedModel>, smallerList: ArrayList<WeatherConvertedModel>): Int{
        for ((index, item) in biggerList.withIndex()){
            if (item != smallerList[index]) return index
        }
        return -1
    }
}

interface OnDataChanged {
    fun locationChanged(weatherConvertedModel: WeatherConvertedModel)
    fun stopRefreshLoader()
}

interface InvalidateAdapterListener {
    fun invalidateAdapter(arrayWeatherList: ArrayList<WeatherConvertedModel>, startPos: Int = 0)
}