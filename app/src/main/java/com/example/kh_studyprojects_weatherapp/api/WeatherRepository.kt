package com.example.kh_studyprojects_weatherapp.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {
    fun getWeatherData(
        params: Map<String, Any>,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = RetrofitInstance.api.getWeatherData(params)
        call.enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onFailure(Throwable("Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
