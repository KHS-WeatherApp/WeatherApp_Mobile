package com.example.kh_studyprojects_weatherapp.domain.model.weather

data class WeatherData(
    val raw: Map<String, Any>,
    val current: WeatherCurrentRaw,
    val hourly: WeatherHourly,
    val daily: WeatherDaily
)

data class WeatherCurrentRaw(
    val time: String?,
    val temperature2m: Double?,
    val apparentTemperature: Double?,
    val relativeHumidity2m: Double?,
    val precipitation: Double?,
    val weatherCode: Int?,
    val windSpeed10m: Double?,
    val raw: Map<String, Any?> = emptyMap()
)

data class WeatherHourly(
    val time: List<String>,
    val temperature2m: List<Double>,
    val apparentTemperature: List<Double>,
    val precipitation: List<Double>,
    val precipitationProbability: List<Int>,
    val weatherCode: List<Int>
)

data class WeatherDaily(
    val time: List<String>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val weatherCode: List<Int>,
    val precipitationSum: List<Double>,
    val precipitationProbabilityMax: List<Int>,
    val apparentTemperatureMax: List<Double>,
    val apparentTemperatureMin: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>
)