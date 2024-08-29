package com.example.kh_studyprojects_weatherapp.weather

class WeatherDailyForecastDto (
    val tvPmPa: String?,            //오전 오후
    val tvDate: String?,            //일자
//    val tvHour: String?,            //시간
    val probability: String?,       //강수확률
    val precipitation: String?,     //강수량
//    val temperature: String?,       //온도
    val minTemperature: String?,    //최소온도
    val maxTemperature: String?,    //최대온도
)