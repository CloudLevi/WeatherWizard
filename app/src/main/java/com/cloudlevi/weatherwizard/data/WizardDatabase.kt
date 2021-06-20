package com.cloudlevi.weatherwizard.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cloudlevi.weatherwizard.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [WeatherConvertedModel::class, CityEntryModel::class, HourlyConverted::class, DailyConverted::class], version = 1)
abstract class WizardDatabase: RoomDatabase() {

    abstract fun wizardDao(): WizardDao

    class Callback @Inject constructor(
        private val database: Provider<WizardDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().wizardDao()

//            applicationScope.launch {
//                val weatherModel = WeatherConvertedModel(
//                    name = "test",
//                    currentTime = "1622489085566",
//                    lat = 45.9678,
//                    lon = 93.1760,
//                    temp = 15.5,
//                    feels_like = 15.3,
//                    main = "main weather",
//                    humidity = 50,
//                    dt = 1622489085566,
//                    sea_level = 2500,
//                    country = "Country",
//                    sunrise = 1622489085366,
//                    sunset = 1622489085866,
//                    description = "Description",
//                    speed = 10.4,
//                    timezone = 2000,
//                )
//
////                var name: String = "",
////                var currentTime: String = "",
////                var lat: Double = 0.0,
////                var lon: Double = 0.0,
////                var temp: Double = 0.0,
////                var feels_like: Double = 0.0,
////                var main: String = "",
////                var humidity: Int = 0,
////                var dt: Long = 0,
////                var sea_level: Int = 0,
////                var country: String = "",
////                var sunrise: Long = 0,
////                var sunset: Long = 0,
////                var description: String = "",
////                var speed: Double = 0.0,
//
//                dao.insertWeatherModel(weatherModel)
//            }
        }

    }
}