package com.cloudlevi.weatherwizard.ui.cityList

import android.content.ContentValues.TAG
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.data.CityEntryModel
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.databinding.QueryCityItemBinding

class CityListQueryAdapter(private val listener: QueryResultListener)
    : ListAdapter<CityEntryModel, CityListQueryAdapter.CityEntryViewHolder>(DiffCallback()) {

    var searchQuery = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityEntryViewHolder {
        return CityEntryViewHolder(QueryCityItemBinding
            .inflate(LayoutInflater
                .from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CityEntryViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class CityEntryViewHolder(private val binding: QueryCityItemBinding)
        : RecyclerView.ViewHolder(binding.root){

        private var firstTime = true

        fun bind(cityEntryModel: CityEntryModel){
            val itemText = "${cityEntryModel.city_ascii}, ${cityEntryModel.country}"
            val spannableString = SpannableString(itemText)
            val fcs = ForegroundColorSpan(Color.WHITE)

            val startEndIndexes = matchDetails(itemText, searchQuery)
            val startIndex = startEndIndexes.first
            val endIndex = startEndIndexes.second

            if(startIndex >= 0 && endIndex >= 0){
                spannableString.setSpan(fcs, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            binding.root.text = spannableString

            if (firstTime){
                binding.root.setOnClickListener {
                    listener.onCityQueryClicked(cityEntryModel)
                }
            }
        }
    }

    fun matchDetails(inputString: String, whatToFind: String): Pair<Int, Int> {
        val matchIndex = inputString.indexOf(whatToFind, 0, ignoreCase = true)
        return Pair(matchIndex, matchIndex + whatToFind.length)
    }

    class DiffCallback : DiffUtil.ItemCallback<CityEntryModel>() {
        override fun areItemsTheSame(oldItem: CityEntryModel, newItem: CityEntryModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CityEntryModel, newItem: CityEntryModel) =
            oldItem == newItem

    }

    interface QueryResultListener{
        fun onCityQueryClicked(cityEntryModel: CityEntryModel)
    }
}