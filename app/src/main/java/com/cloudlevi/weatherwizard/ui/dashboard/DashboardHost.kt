package com.cloudlevi.weatherwizard.ui.dashboard

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.cloudlevi.weatherwizard.MainActivity
import com.cloudlevi.weatherwizard.MainViewModel
import com.cloudlevi.weatherwizard.R
import com.cloudlevi.weatherwizard.data.WeatherConvertedModel
import com.cloudlevi.weatherwizard.databinding.FragmentDashboardHostBinding
import com.cloudlevi.weatherwizard.ui.cityList.CityListListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardFragmentEvent.*
import com.google.android.material.tabs.TabLayout
import kotlin.math.abs

@AndroidEntryPoint
class DashboardHost:
    Fragment(R.layout.fragment_dashboard_host),
    DashboardFragmentItem.HostListener,
    InvalidateAdapterListener,
    CityListListener{

    private val viewModel: DashboardFragmentViewModel by activityViewModels()
    private lateinit var binding: FragmentDashboardHostBinding
    private lateinit var viewPagerAdapter: DashboardViewPagerAdapter
    var bgColorList = HashMap<Int, Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDashboardHostBinding.bind(view)

        binding.apply {

            viewModel.location.observe(viewLifecycleOwner) {
                viewModel.onLocationUpdated(it)
            }

            (activity as MainActivity).currentTimeLiveData.observe(viewLifecycleOwner) {
                viewModel.timeUpdated()
            }

            // Observe changes in Room
            viewModel.weatherLiveData.observe(viewLifecycleOwner) {
                viewModel.listOfWeather = ArrayList(it)
                if (this@DashboardHost::viewPagerAdapter.isInitialized) {
                    viewPagerAdapter.submitList(it)
                }
                else {
                    createViewPagerAdapter(it)
                }

                attachTabLayout()
            }

        }
    }

    private fun attachTabLayout(){
        TabLayoutMediator(binding.tabLayout, binding.dashBoardViewPager){ tab, position ->
            when(position){
                0 -> tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation)
                viewPagerAdapter.itemCount - 1 -> tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
                else -> tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.circle_slider)
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position ?: 0
                if (abs(binding.dashBoardViewPager.currentItem - tabPosition) > 2)
                    tabPosition.let { binding.dashBoardViewPager.setCurrentItem(it, false) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.dashBoardViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != viewPagerAdapter.itemCount - 1) {
                    binding.tabLayoutGroup.background =
                        getDrawableColor(bgColorList[position] ?: R.color.day)
                }
            }
        })
    }

    private fun createViewPagerAdapter(weatherList: List<WeatherConvertedModel>, startPos: Int = 0){
        viewPagerAdapter = DashboardViewPagerAdapter(this@DashboardHost, weatherList)
        viewPagerAdapter.notifyDataSetChanged()
        binding.dashBoardViewPager.adapter = viewPagerAdapter
        binding.dashBoardViewPager.setCurrentItem(startPos, true)
    }

    override fun onChangeBgListener(position: Int, color: Int) {
        bgColorList[position] = color
        if(binding.dashBoardViewPager.currentItem == position)
            binding.tabLayoutGroup.background = getDrawableColor(color)
    }

    private fun getDrawableColor(color: Int): Drawable{
        return ContextCompat.getDrawable(requireContext(), color)!!
    }

    override fun invalidateAdapter(arrayWeatherList: ArrayList<WeatherConvertedModel>, startPos: Int) {
        (activity as MainActivity).hideKeyboard(requireView())
        createViewPagerAdapter(arrayWeatherList, startPos)
    }

    override fun onCityClicked(position: Int, smoothScroll: Boolean) {
        binding.dashBoardViewPager.setCurrentItem(position, smoothScroll)
    }

}