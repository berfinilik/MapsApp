package com.berfinilik.googlemaps.network

import com.berfinilik.googlemaps.GeoDBService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private fun createRetrofit(baseUrl: String, headers: Map<String, String>): Retrofit {
        val client = httpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                headers.forEach { (key, value) ->
                    requestBuilder.addHeader(key, value)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createGeoDBService(): GeoDBService {
        val headers = mapOf(
            "X-RapidAPI-Key" to "9d2a78f44fmsh6577d1f5a25ba40p172c57jsn9ca33f54595b",
            "X-RapidAPI-Host" to "wft-geo-db.p.rapidapi.com"
        )
        return createRetrofit("https://wft-geo-db.p.rapidapi.com/v1/geo/", headers).create(
            GeoDBService::class.java)
    }

    fun createOverpassService(): OverpassApiService {
        return Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApiService::class.java)
    }
}
