package com.berfinilik.googlemaps

data class DataItem(
    val id: Int,
    val wikiDataId: String?,
    val type: String?,
    val city: String?,
    val name: String?,
    val country: String?,
    val code: String?,
    val region: String?,
    val regionCode: String?,
    val regionWdId: String?,
    val latitude: Double?,
    val longitude: Double?,
    val population: Int?
)