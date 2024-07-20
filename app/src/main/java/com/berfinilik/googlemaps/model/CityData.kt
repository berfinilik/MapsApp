package com.berfinilik.googlemaps

data class CityData(
    val id: String,
    val wikiDataId: String,
    val type: String,
    val city: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val region: String,
    val regionCode: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int,
    val elevationMeters: Int?
)