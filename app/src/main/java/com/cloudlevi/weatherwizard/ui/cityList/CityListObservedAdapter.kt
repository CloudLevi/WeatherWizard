package com.cloudlevi.weatherwizard.ui.cityList

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.R
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.databinding.ObservedCityItemBinding
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardFragmentItem
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardHost
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CityListObservedAdapter(
    private val listener: CityListListener,
    private var currentList: ArrayList<WeatherConvertedModel>
) :
    RecyclerView.Adapter<CityListObservedAdapter.CityListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityListItemViewHolder {
        return CityListItemViewHolder(
            ObservedCityItemBinding
                .inflate(
                    LayoutInflater
                        .from(parent.context), parent, false), listener)
    }

    override fun onBindViewHolder(holder: CityListItemViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem)
    }

    inner class CityListItemViewHolder(
        private val binding: ObservedCityItemBinding,
        private val listener: CityListListener):
    RecyclerView.ViewHolder(binding.root),
    View.OnClickListener{

        init {
            binding.root.setOnClickListener(this@CityListItemViewHolder)
        }

        fun bind(weatherConvertedModel: WeatherConvertedModel) {
            binding.apply {
                val tempString = "${weatherConvertedModel.temp.toInt()}°C"

                weatherBG.setImageResource(determineBG(weatherConvertedModel))
                timeTV.text = weatherConvertedModel.currentTime
                cityTV.text = weatherConvertedModel.name
                tempTV.text = tempString
            }
        }

        override fun onClick(v: View?) {
            listener.onCityClicked(adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitListNew(newWeatherList: List<WeatherConvertedModel>, position: Int = -1) {

        if (currentList == newWeatherList) {
            Log.d(TAG, "Same weather list!")
            return
        }
        else{
            if (currentList.size != newWeatherList.size) {
                Log.d(TAG, "submitListNew: First if")
                if (currentList.size == newWeatherList.size + 1 && position != -1) {
                    currentList = ArrayList(newWeatherList)
                    notifyItemRemoved(position)
                    Log.d(TAG, "Notify Item removed $position")
                    return
                }
                else currentList = ArrayList(newWeatherList)
            } else {
                if (currentList.size != 0) {
                    //Compare old items with new
                    for ((index, weatherOld) in currentList.withIndex()) {
                        val weather = newWeatherList[index]
                        if (weather != currentList[index]) {
                            currentList[index] = weather
                        }
                    }

                } else {
                    currentList = ArrayList(newWeatherList)
                }
            }
        }

        Log.d(TAG, "Notify Data set changed")
        notifyDataSetChanged()
    }

    private fun determineBG(weatherModel: WeatherConvertedModel): Int {
        val currentTime = weatherModel.dt
        val sunriseTime = weatherModel.sunrise
        val sunsetTime = weatherModel.sunset
        return when {
            currentTime in (sunriseTime + 1) until sunsetTime -> {
                R.drawable.day_sunny_cropped
            }
            currentTime <= sunriseTime || currentTime >= sunsetTime -> {
                R.drawable.night_sunny_cropped
            }
            else -> {
                R.drawable.day_sunny_cropped
            }
        }
    }

    interface CityListListener {
        fun onCityClicked(position: Int)
    }
}