package me.ryanpierce.mapsstateflowdemo

sealed class GpsResult {

    object Standby : GpsResult()

    data class NewLocation(val location: Location) : GpsResult()
}