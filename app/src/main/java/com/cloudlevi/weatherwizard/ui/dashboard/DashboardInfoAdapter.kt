package com.cloudlevi.weatherwizard.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.data.DailyConverted
import com.cloudlevi.weatherwizard.data.WeatherDashboardModel
import com.cloudlevi.weatherwizard.databinding.DashboardInfoItemBinding

class DashboardInfoAdapter(
    var currentList: ArrayList<WeatherDashboardModel>
): RecyclerView.Adapter<DashboardInfoAdapter.DashboardInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardInfoViewHolder {
        return DashboardInfoViewHolder(
            DashboardInfoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DashboardInfoViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitList(newDashboardList: List<WeatherDashboardModel>) {

        if (currentList.size != newDashboardList.size) {
            currentList = ArrayList(newDashboardList)
        } else {
            if (currentList.size != 0) {
                //Compare old items with new
                for ((index, weatherOld) in currentList.withIndex()) {
                    val weather = newDashboardList[index]
                    if (weather != currentList[index]) {
                        currentList[index] = weather
                    }
                }
            } else {
                currentList = ArrayList(newDashboardList)
            }
        }

        notifyDataSetChanged()
    }


    inner class DashboardInfoViewHolder(private val binding: DashboardInfoItemBinding
    ): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(weatherDashboardModel: WeatherDashboardModel){
            binding.apply {
                titleTV.text = weatherDashboardModel.titleText
                valueTV.text = weatherDashboardModel.valueText
            }
        }
    }
}