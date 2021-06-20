package com.cloudlevi.weatherwizard

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.cloudlevi.weatherwizard.api.ApiService
import com.cloudlevi.weatherwizard.data.LocationModel
import com.cloudlevi.weatherwizard.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private lateinit var minuteUpdateReceiver: BroadcastReceiver

    private val mainActivityViewModel: MainViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    var currentTimeLiveData = MutableLiveData<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
            return
        }

        mainActivityViewModel.weatherListLiveData.observe(this){
            mainActivityViewModel.currentWeatherList = ArrayList(it)
        }

        setLocationListener()
    }

    override fun onLocationChanged(location: Location) {
        setLastLocation(location.latitude.toString(), location.longitude.toString())
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) setLocationListener()
                else ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setLocationListener(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10000f, this)
    }

    private fun setLastLocation(lat: String, lon: String) =
        mainActivityViewModel.saveLocation(LocationModel(latitude = lat, longitude = lon))

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupViews(){

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
    }

    fun registerMinuteListener(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_TICK)

        minuteUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {

                mainActivityViewModel.onTimeChanged()

            }
        }

        registerReceiver(minuteUpdateReceiver, intentFilter)
    }

    fun hideKeyboard(view: View){
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        registerMinuteListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(minuteUpdateReceiver)
    }

}

val retroWeatherByCoordinates: Retrofit = Retrofit.Builder()
        .baseUrl("http://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

val apiServiceWeatherByCoordinates: ApiService = retroWeatherByCoordinates.create(ApiService::class.java)

const val mainActivityAppID = "b7343e5385890df47d8c46ba272b1a7e"
const val mainActivityUnits = "metric"
