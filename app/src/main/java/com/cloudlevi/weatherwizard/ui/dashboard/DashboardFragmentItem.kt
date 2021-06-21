package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudlevi.weatherwizard.MainActivity
import com.cloudlevi.weatherwizard.MainViewModel
import com.cloudlevi.weatherwizard.R
import com.cloudlevi.weatherwizard.data.DailyConverted
import com.cloudlevi.weatherwizard.data.HourlyConverted
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.data.WeatherLocationModel
import com.cloudlevi.weatherwizard.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardFragmentEvent.*
import kotlinx.coroutines.flow.collect
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DashboardFragmentItem(private val listener: DashboardHost) : Fragment(R.layout.fragment_dashboard), OnDataChanged {

    companion object {
        const val BUNDLE_POSITION = "position"
        const val BUNDLE_WEATHER_PARCELABLE = "weather_converted"

        fun getInstance(position: Int, weatherConverted: WeatherConvertedModel, listener: DashboardHost): Fragment {
            val apartmentPageSliderItem = DashboardFragmentItem(listener)
            val bundle = Bundle()
            bundle.putInt(BUNDLE_POSITION, position)
            bundle.putParcelable(BUNDLE_WEATHER_PARCELABLE, weatherConverted)
            apartmentPageSliderItem.arguments = bundle

            return apartmentPageSliderItem
        }
    }

    //    private val viewModel: DashboardFragmentViewModel by viewModels()
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardFragmentViewModel by activityViewModels()

    private var hourlyForecastAdapter = HourlyForecastAdapter(arrayListOf(HourlyConverted()))
    private var dailyForecastAdapter = DailyForecastAdapter(arrayListOf(DailyConverted()))
    private lateinit var dashboardInfoAdapter: DashboardInfoAdapter

    var position = -1

    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onResume() {
        activityViewModel.onTimeChanged()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDashboardBinding.bind(view)

        val weatherModel: WeatherConvertedModel? =
            requireArguments().getParcelable(BUNDLE_WEATHER_PARCELABLE)
        dashboardInfoAdapter = DashboardInfoAdapter(viewModel
            .getDashboardInfoList(weatherModel?: WeatherConvertedModel()))

        updateUI(weatherModel)

        position = requireArguments().getInt(BUNDLE_POSITION)

        viewModel.updateWeatherModel(viewModel.listOfWeather[position])

        viewModel.getForecast(viewModel.listOfWeather[position])

        viewModel.titleLiveData.observe(viewLifecycleOwner){
            binding.cityTV.text = it
        }

        viewModel.weatherLiveData.observe(viewLifecycleOwner){
            if (binding.currentTimeTV.text.toString() != it[position].currentTime)
                binding.currentTimeTV.text = it[position].currentTime
        }

        val verticalGridLayoutManager = object : GridLayoutManager(requireContext(), 2, VERTICAL, false){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        binding.dashboardInfoRecycler.layoutManager = verticalGridLayoutManager
        binding.dashboardInfoRecycler.adapter = dashboardInfoAdapter

        binding.hourlyForecastRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyForecastRecycler.setHasFixedSize(true)

        binding.hourlyForecastRecycler.adapter = hourlyForecastAdapter

        val verticalLayoutManager = object : LinearLayoutManager(requireContext(), VERTICAL, false){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        binding.dailyForecastRecycler.layoutManager = verticalLayoutManager
        binding.dailyForecastRecycler.setHasFixedSize(true)

        binding.dailyForecastRecycler.adapter = dailyForecastAdapter

        //Observe Room changes in Hourly Forecast
        viewModel.hourlyForecastLiveData.observe(viewLifecycleOwner){
            viewModel.currentHourlyList = ArrayList(it)
            val listToSubmit = viewModel.getFragmentHourlyForecast(position)

            if (hourlyForecastAdapter.currentList != listToSubmit && listToSubmit.isNotEmpty()) {
                hourlyForecastAdapter.submitList(listToSubmit)
            }
        }

        //Observe Room changes in Daily Forecast
        viewModel.dailyForecastLiveData.observe(viewLifecycleOwner){
            viewModel.currentDailyList = ArrayList(it)
            val listToSubmit = viewModel.getFragmentDailyForecast(position)

            if (dailyForecastAdapter.currentList != listToSubmit && listToSubmit.isNotEmpty()) {
                dailyForecastAdapter.submitList(listToSubmit)
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updateWeatherModel(viewModel.listOfWeather[position], true)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.dashboardFragmentEvent.collect { event ->
                when(event){
                    is NotifyWeatherModelSaved -> {
                        dashboardInfoAdapter.submitList(viewModel.getDashboardInfoList(event.weatherModel))
                    }
                    is SendToastMessage -> sendToastMessage(event.message)
                    is StopRefreshLoader -> stopRefreshing()
                }
            }
        }
    }

    private fun sendToastMessage(message: String){
        stopRefreshing()
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun updateUI(weatherModel: WeatherConvertedModel?) {
        if (weatherModel != null && this::binding.isInitialized) {
            binding.apply {
                stopRefreshing()

                cityTV.text = weatherModel.name
                tempTV.text = addTextToString("", weatherModel.temp.toInt(), "°C")
                if (weatherModel.main.isNotEmpty())
                    descriptionTV.text = weatherModel.main
                else descriptionTV.text = weatherModel.description

                currentTimeTV.text = weatherModel.currentTime

                backgroundIV.setImageResource(determineBG(weatherModel))
            }
        }
    }

    private fun determineBG(weatherModel: WeatherConvertedModel): Int {
        val currentTime = weatherModel.dt
        val sunriseTime = weatherModel.sunrise
        val sunsetTime = weatherModel.sunset
        return when {
            currentTime in (sunriseTime + 1) until sunsetTime -> {
                listener.onChangeBgListener(requireArguments().getInt(BUNDLE_POSITION), R.drawable.tab_bg_day)
                binding.frameLayout.background = ContextCompat.getDrawable(requireContext(), R.color.main_color_bg_day)
                R.drawable.day_sunny
            }
            currentTime <= sunriseTime || currentTime >= sunsetTime -> {
                listener.onChangeBgListener(requireArguments().getInt(BUNDLE_POSITION), R.drawable.tab_bg_night)
                binding.frameLayout.background = ContextCompat.getDrawable(requireContext(), R.color.main_color_bg_night)
                R.drawable.night_sunny
            }
            else -> {
                listener.onChangeBgListener(requireArguments().getInt(BUNDLE_POSITION), R.drawable.tab_bg_night)
                binding.frameLayout.background = ContextCompat.getDrawable(requireContext(), R.color.main_color_bg_night)
                R.drawable.day_sunny
            }
        }
    }

    private fun addTextToString(frontText: String, requestedString: Any, rearText: String): String {
        return "$frontText$requestedString$rearText"
    }

    override fun locationChanged(weatherConvertedModel: WeatherConvertedModel) {
        if (this::dashboardInfoAdapter.isInitialized)
            dashboardInfoAdapter.submitList(viewModel.getDashboardInfoList(weatherConvertedModel))
//        updateUI(weatherConvertedModel)
    }

    private fun stopRefreshing() {
        if (this::binding.isInitialized && binding.swipeRefreshLayout.isRefreshing)
            binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun stopRefreshLoader() {
        stopRefreshing()
    }

    interface HostListener{
        fun onChangeBgListener(position: Int, color: Int)
    }

}