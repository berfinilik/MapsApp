package com.berfinilik.googlemaps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.berfinilik.googlemaps.DataItem
import com.berfinilik.googlemaps.repository.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val mapsRepository: MapsRepository
) : ViewModel() {

    private val _countries = MutableLiveData<List<DataItem>>()
    val countries: LiveData<List<DataItem>> get() = _countries

    private val _cities = MutableLiveData<List<DataItem>>()
    val cities: LiveData<List<DataItem>> get() = _cities

    private val _geoPoint = MutableLiveData<GeoPoint>()
    val geoPoint: LiveData<GeoPoint> get() = _geoPoint

    fun fetchCountries() {
        mapsRepository.getCountries { result ->
            _countries.value = result
        }
    }

    fun fetchCities(countryCode: String) {
        mapsRepository.getCities(countryCode) { result ->
            _cities.value = result
        }
    }

    fun fetchCityCoordinates(city: String) {
        mapsRepository.getCityCoordinates(city) { result ->
            _geoPoint.value = result.latitude?.let { result.longitude?.let { it1 ->
                GeoPoint(it,
                    it1
                )
            } }
        }
    }
}
