package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.data.HourlyConverted
import com.cloudlevi.weatherwizard.databinding.ForecastHourlyItemBinding
import kotlin.math.floor

class HourlyForecastAdapter(
    var currentList: ArrayList<HourlyConverted>
): RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        return HourlyForecastViewHolder(
            ForecastHourlyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem, position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitList(newHourlyList: List<HourlyConverted>) {

        if (currentList.size != newHourlyList.size) {
            currentList = ArrayList(newHourlyList)
        } else {
            if (currentList.size != 0) {
                //Compare old items with new
                for ((index, weatherOld) in currentList.withIndex()) {
                    val weather = newHourlyList[index]
                    if (weather != currentList[index]) {
                        currentList[index] = weather
                    }
                }

            } else {
                currentList = ArrayList(newHourlyList)
            }
        }

        notifyDataSetChanged()
    }

    inner class HourlyForecastViewHolder(
        private val binding: ForecastHourlyItemBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(hourlyConverted: HourlyConverted, position: Int){
            binding.apply {
                val tempString = "${floor(hourlyConverted.hourlyTemp).toInt()}°"
                timeTV.text = hourlyConverted.hourlyTime
                weatherImage.setImageResource(getDrawableByName(hourlyConverted.hourlyIcon, binding.root.context))
                val popString = if (hourlyConverted.hourlyPrecipitation > 50)
                    "${hourlyConverted.hourlyPrecipitation}%"
                else ""

                precipitationTV.text = popString
                tempTV.text = tempString
            }
        }
    }

    private fun getDrawableByName(name: String, context: Context): Int{
        return context.resources
            .getIdentifier("ic_${name}", "drawable", context.packageName)
    }
}