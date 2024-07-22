package com.berfinilik.googlemaps

data class DataItem(
    val id: Int = 0,
    val wikiDataId: String? = null,
    val type: String? = null,
    val city: String? = null,
    val name: String? = null,
    val country: String? = null,
    val code: String? = null,
    val region: String? = null,
    val regionCode: String? = null,
    val regionWdId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val population: Int? = null
)
