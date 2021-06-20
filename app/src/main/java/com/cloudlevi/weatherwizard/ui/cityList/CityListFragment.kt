package com.cloudlevi.weatherwizard.ui.cityList

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.MainActivity
import com.cloudlevi.weatherwizard.MainViewModel
import com.cloudlevi.weatherwizard.R
import com.cloudlevi.weatherwizard.data.CityEntryModel
import com.cloudlevi.weatherwizard.databinding.FragmentCityListBinding
import com.cloudlevi.weatherwizard.ui.dashboard.DashboardFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


@AndroidEntryPoint
class CityListFragment(private val listener: CityListListener):
    Fragment(R.layout.fragment_city_list),
    CityListQueryAdapter.QueryResultListener,
    CityListObservedAdapter.CityListListener,
    SimpleCallbackListener{

    companion object {
        fun getInstance(listener: CityListListener): Fragment =
            CityListFragment(listener)
    }

    private val viewModel: CityListViewModel by viewModels()
    private val dashboardViewModel: DashboardFragmentViewModel by activityViewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentCityListBinding
    private lateinit var cityListObservedAdapter: CityListObservedAdapter
    private var cityListQueryAdapter = CityListQueryAdapter(this)

    private lateinit var swipeHelper: SwipeHelper

    private var cancelSearch = false

    override fun onResume() {
        activityViewModel.onTimeChanged()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCityListBinding.bind(view)

        binding.apply {

            searchEditText.addTextChangedListener {
                if (it != null) viewModel.onQueryChanged(it.toString().trim(), cancelSearch)
                else viewModel.onQueryChanged("", cancelSearch)
            }

            searchEditText.setOnFocusChangeListener { editTextView, hasFocus ->
                if(hasFocus){
                    cancelTV.visibility = View.VISIBLE
                    cancelSearch = false
                }
            }

            cancelTV.setOnClickListener {
                cancelClick()
            }

            observedLocationRecycler.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        viewModel.weatherListLiveData.observe(viewLifecycleOwner) {
            if (this::cityListObservedAdapter.isInitialized) {
                if (viewModel.deletedPosition != -1) {
                    Log.d(TAG, "live data received at pos ${viewModel.deletedPosition}")
                    cityListObservedAdapter.submitListNew(ArrayList(it), viewModel.deletedPosition)
                }
                else cityListObservedAdapter.submitListNew(ArrayList(it))

//                viewModel.deletedPosition = -1
            }
            else {
                cityListObservedAdapter = CityListObservedAdapter(this, ArrayList(it))
                binding.observedLocationRecycler.adapter = cityListObservedAdapter
            }
        }

        viewModel.queryCitiesLiveData.observe(viewLifecycleOwner) {
            if (binding.searchEditText.isFocused && binding.searchEditText.text.toString() != ""){
                cityListQueryAdapter.searchQuery = viewModel.searchQuery.value?: ""
                cityListQueryAdapter.submitList(it)
                cityListQueryAdapter.notifyDataSetChanged()
                binding.observedLocationRecycler.adapter = cityListQueryAdapter
            }
        }
        
        val simpleCallback = ItemTouchHelperCallback(this)
        ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.observedLocationRecycler)

    }

    private fun cancelClick(){
        binding.cancelTV.visibility = View.GONE
        (activity as MainActivity).hideKeyboard(requireView())
        cancelSearch = true
        binding.searchEditText.setText("")
        binding.searchEditText.clearFocus()
        binding.observedLocationRecycler.adapter = cityListObservedAdapter
    }

    override fun onCityQueryClicked(cityEntryModel: CityEntryModel) {
        cancelClick()
        dashboardViewModel.addObservedLocation(cityEntryModel)
    }

    override fun onPause() {
        cityListObservedAdapter.notifyItemChanged(viewModel.currentItemSwiped)
        super.onPause()
    }

    override fun onCityClicked(position: Int) {
        listener.onCityClicked(position, true)
    }

    override fun onItemSwiped(position: Int) {
        viewModel.onDeleteClicked(position)
//        cityListObservedAdapter.notifyItemRemoved(position)
    }
}

interface CityListListener{
    fun onCityClicked(position: Int, smoothScroll: Boolean)
}