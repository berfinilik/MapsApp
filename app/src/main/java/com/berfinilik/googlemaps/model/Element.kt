package com.berfinilik.googlemaps.model

data class Element(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Tags
)
