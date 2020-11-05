package me.ryanpierce.mapsstateflowdemo

import kotlinx.coroutines.flow.flowOf

class MapsDataSource {

    companion object {
        val CHICAGO = "Chicago" at (41.8925 x -87.6250)
    }

    val locations = flowOf(
        "Navy Pier"         at (41.8917 x -87.6090),
        "Lincoln Park Zoo"  at (41.9210 x -87.6335),
        "Adler Planetarium" at (41.8663 x -87.6069)
    )
}