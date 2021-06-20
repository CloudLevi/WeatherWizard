package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.data.DailyConverted
import com.cloudlevi.weatherwizard.data.HourlyConverted
import com.cloudlevi.weatherwizard.databinding.ForecastDailyItemBinding

class DailyForecastAdapter(
    var currentList: ArrayList<DailyConverted>
): RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        return DailyForecastViewHolder(
            ForecastDailyItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitList(newDailyList: List<DailyConverted>) {

        if (currentList.size != newDailyList.size) {
            currentList = ArrayList(newDailyList)
        } else {
            if (currentList.size != 0) {
                //Compare old items with new
                for ((index, weatherOld) in currentList.withIndex()) {
                    val weather = newDailyList[index]
                    if (weather != currentList[index]) {
                        currentList[index] = weather
                    }
                }

            } else {
                currentList = ArrayList(newDailyList)
            }
        }

        notifyDataSetChanged()
    }


    inner class DailyForecastViewHolder(private val binding: ForecastDailyItemBinding
    ): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(dailyConverted: DailyConverted){
            binding.apply {
                dayTV.text = dailyConverted.dailyText
                weatherImage.setImageResource(getDrawableByName(dailyConverted.dailyIcon, binding.root.context))
                val precipitationString = if (dailyConverted.dailyPrecipitation > 50)
                    "${dailyConverted.dailyPrecipitation}%"
                else ""
                precipitationTV.text = precipitationString
                val tempMax = dailyConverted.dailyTempMax.toInt()
                val tempMin = dailyConverted.dailyTempMin.toInt()

                val tempMaxString = if (tempMax.toString().length < 2) "  $tempMax"
                else "$tempMax"

                val tempMinString = if (tempMin.toString().length < 2) "  $tempMin"
                else "$tempMin"

                tempMaxTV.text = tempMaxString
                tempMinTV.text = tempMinString
            }
        }
    }

    private fun getDrawableByName(name: String, context: Context): Int{
        return context.resources
            .getIdentifier("ic_${name}", "drawable", context.packageName)
    }
}