package com.berfinilik.googlemaps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.berfinilik.googlemaps.DataItem
import com.berfinilik.googlemaps.GeoDBResponse
import com.berfinilik.googlemaps.RetrofitClient
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel : ViewModel() {

    private val _countries = MutableLiveData<List<DataItem>>()
    val countries: LiveData<List<DataItem>> get() = _countries

    private val _cities = MutableLiveData<List<DataItem>>()
    val cities: LiveData<List<DataItem>> get() = _cities

    private val _geoPoint = MutableLiveData<GeoPoint>()
    val geoPoint: LiveData<GeoPoint> get() = _geoPoint

    fun fetchCountries() {
        RetrofitClient.instance.getCountries().enqueue(object : Callback<GeoDBResponse> {
            override fun onResponse(call: Call<GeoDBResponse>, response: Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    _countries.value = response.body()?.data ?: emptyList()
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<GeoDBResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    fun fetchCities(countryCode: String) {
        RetrofitClient.instance.getCities(countryCode).enqueue(object : Callback<GeoDBResponse> {
            override fun onResponse(call: Call<GeoDBResponse>, response: Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    _cities.value = response.body()?.data ?: emptyList()
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<GeoDBResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    fun fetchCityCoordinates(city: String) {
        RetrofitClient.instance.getCityCoordinates(city).enqueue(object : Callback<GeoDBResponse> {
            override fun onResponse(call: Call<GeoDBResponse>, response: Response<GeoDBResponse>) {
                if (response.isSuccessful) {
                    val cityData = response.body()?.data?.firstOrNull()
                    cityData?.let {
                        _geoPoint.value = GeoPoint(it.latitude ?: 0.0, it.longitude ?: 0.0)
                    }
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<GeoDBResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}
