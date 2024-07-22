package com.berfinilik.googlemaps.repository

import com.berfinilik.googlemaps.DataItem
import com.berfinilik.googlemaps.GeoDBResponse
import com.berfinilik.googlemaps.GeoDBService
import javax.inject.Inject

class MapsRepository @Inject constructor(
    private val geoDBService: GeoDBService
) {
    fun getCountries(callback: (List<DataItem>) -> Unit) {
        geoDBService.getCountries().enqueue(object : retrofit2.Callback<GeoDBResponse> {
            override fun onResponse(call: retrofit2.Call<GeoDBResponse>, response: retrofit2.Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.data ?: emptyList())
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: retrofit2.Call<GeoDBResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun getCities(countryCode: String, callback: (List<DataItem>) -> Unit) {
        geoDBService.getCities(countryCode).enqueue(object : retrofit2.Callback<GeoDBResponse> {
            override fun onResponse(call: retrofit2.Call<GeoDBResponse>, response: retrofit2.Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.data ?: emptyList())
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: retrofit2.Call<GeoDBResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun getCityCoordinates(city: String, callback: (DataItem) -> Unit) {
        geoDBService.getCityCoordinates(city).enqueue(object : retrofit2.Callback<GeoDBResponse> {
            override fun onResponse(call: retrofit2.Call<GeoDBResponse>, response: retrofit2.Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.data?.firstOrNull() ?: DataItem())
                } else {
                    callback(DataItem())
                }
            }

            override fun onFailure(call: retrofit2.Call<GeoDBResponse>, t: Throwable) {
                callback(DataItem())
            }
        })
    }
}
