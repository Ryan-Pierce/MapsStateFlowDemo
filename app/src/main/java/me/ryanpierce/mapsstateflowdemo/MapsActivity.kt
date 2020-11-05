package me.ryanpierce.mapsstateflowdemo

import android.animation.AnimatorSet
import android.animation.ValueAnimator.ofFloat
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import me.ryanpierce.mapsstateflowdemo.MapsDataSource.Companion.CHICAGO

class MapsActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var viewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val repository = MapsRepository(MapsDataSource())
        viewModel = ViewModelProvider(this, MapsViewModel.FACTORY(repository))
            .get(MapsViewModel::class.java)

        tourNormalMap()
        tourHybridMap()
        tourWebView()
    }

    fun tourNormalMap() {
        val normalMap = supportFragmentManager.findFragmentById(R.id.normalMap) as SupportMapFragment
        normalMap.getMapAsync { map ->
            val marker = map.addMarker(MarkerOptions().position(.0 x .0))
            observeLocations(
                viewModel.gpsResults,
                map,
                setup = {
                    mapType = MAP_TYPE_NORMAL
                    isBuildingsEnabled = true
                    moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(
                                CHICAGO.coordinate, // Coordinate
                                13.2f, // Zoom
                                80f, // Tilt
                                0f // Bearing
                            )
                        )
                    )
                },
                onEachLocation = { location ->
                    marker.moveTo(location.coordinate)
                }
            )
        }
    }

    fun tourHybridMap() {
        val hybridMap = supportFragmentManager.findFragmentById(R.id.hybridMap) as SupportMapFragment
        hybridMap.getMapAsync { map ->
            observeLocations(
                viewModel.gpsResults,
                map,
                setup = {
                    mapType = MAP_TYPE_HYBRID
                    isBuildingsEnabled = true
                },
                onEachLocation = { location ->
                    map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(
                                location.coordinate, // Coordinate
                                15f, // Zoom
                                80f, // Tilt
                                270f // Bearing
                            )
                        )
                    )
                }
            )
        }
    }

    fun CoroutineScope.tourWebView() {
        val webView = findViewById<WebView>(R.id.information).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) =
                    false
            }
        }
        viewModel
            .gpsResults
            .filterIsInstance<GpsResult.NewLocation>()
            .map { it.location }
            .onEach { webView.loadUrl("https://www.google.com/search?q=${it.name}") }
            .launchIn(this)
    }

    fun CoroutineScope.observeLocations(
        gpsResults: StateFlow<GpsResult>,
        map: GoogleMap,
        setup: GoogleMap.() -> Unit,
        onEachLocation: suspend (Location) -> Unit
    ) = gpsResults
            .onStart { map.setup() }
            .filterIsInstance<GpsResult.NewLocation>()
            .map { it.location }
            .onEach { onEachLocation(it) }
            .launchIn(this)

    /**
     * Animates the marker after moving to a new location
     */
    private fun Marker.moveTo(coordinate: Coordinate) {
        val marker = this
        val before = position.latitude x position.longitude
        val after = coordinate.latitude x coordinate.longitude

        // Animate movement
        val latitudeAnimation = ofFloat(before.latitude.toFloat(), after.latitude.toFloat()).apply {
            addUpdateListener { animation ->
                val latitude = (animation.animatedValue as Float).toDouble()
                val longitude = marker.position.longitude
                marker.position = latitude x longitude
            }
        }
        val longitudeAnimation = ofFloat(before.longitude.toFloat(), after.longitude.toFloat()).apply {
            addUpdateListener { animation ->
                val latitude = marker.position.latitude
                val longitude = (animation.animatedValue as Float).toDouble()
                marker.position = latitude x longitude
            }
        }
        AnimatorSet().apply {
            playTogether(latitudeAnimation, longitudeAnimation)
            duration = 500
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}