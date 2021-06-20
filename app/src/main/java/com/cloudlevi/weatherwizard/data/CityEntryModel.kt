package com.cloudlevi.weatherwizard.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "world_cities")
data class CityEntryModel constructor(
    var city: String?,
    var city_ascii: String?,
    var lat: Double?,
    var lng: Double?,
    var country: String?,
    var population: Int?,
    var iso2: String?,
    var iso3: String?,
    var admin_name: String?,
    var capital: String?,
    @PrimaryKey
    var id: Int
) {
}