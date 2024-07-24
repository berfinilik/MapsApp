package com.berfinilik.googlemaps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoDBService {
    @GET("countries")
    fun getCountries(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 10
    ): Call<GeoDBResponse>

    @GET("cities")
    fun getCities(
        @Query("countryIds") countryCode: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 10
    ): Call<GeoDBResponse>

    @GET("cities")
    fun getCityCoordinates(
        @Query("namePrefix") cityName: String,
        @Query("limit") limit: Int = 1
    ): Call<GeoDBResponse>
}