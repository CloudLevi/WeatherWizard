package com.cloudlevi.weatherwizard.data

data class WeatherLocationModel constructor(
    var base: String = "",
    var clouds: Clouds = Clouds(),
    var cod: Int = 0,
    var coord: Coord = Coord(),
    var dt: Long = 0,
    var id: Int = 0,
    var main: Main = Main(),
    var name: String = "",
    var sys: Sys = Sys(),
    var timezone: Int = 0,
    var visibility: Int = 0,
    var weather: List<Weather> = listOf(Weather()),
    var wind: Wind = Wind(),
    var currentTime: String = "",
    var db_id: Int = -10
)

data class Clouds(var all: Int = 0)

data class Coord( var lat: Double = 0.0, var lon: Double = 0.0)

data class Main(
    var feels_like: Double = 0.0,
    var grnd_level: Int = 0,
    var humidity: Int = 0,
    var pressure: Int = 0,
    var sea_level: Int = 0,
    var temp: Double = 0.0,
    var temp_max: Double = 0.0,
    var temp_min: Double = 0.0
)

data class Sys( var country: String = "", var sunrise: Long = 0, var sunset: Long = 0)

data class Weather(
    var description: String = "",
    var icon: String = "",
    var id: Int = 0,
    var main: String = ""
)

data class Wind( var deg: Int = 0, var gust: Double = 0.0, var speed: Double = 0.0)