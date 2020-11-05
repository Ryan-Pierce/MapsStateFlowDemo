package me.ryanpierce.mapsstateflowdemo

data class Location(val name: String, val coordinate: Coordinate)

infix fun String.at(that: Coordinate) = Location(this, that)