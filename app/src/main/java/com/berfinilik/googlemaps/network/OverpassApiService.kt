package com.berfinilik.googlemaps.network

import com.berfinilik.googlemaps.model.OverpassResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("interpreter")
    fun getRestaurants(
        @Query("data") data: String
    ): Call<OverpassResponse>
}
