package me.ryanpierce.mapsstateflowdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class MapsViewModel(val repository: MapsRepository) : ViewModel() {

    companion object {
        val FACTORY = singleArgumentViewModelFactory(::MapsViewModel)
    }

    private val _gpsResults = MutableStateFlow<GpsResult>(GpsResult.Standby)
    val gpsResults: StateFlow<GpsResult> get() = _gpsResults

    init {
        viewModelScope.broadcastConflatedLocations(repository.gpsLocations)
    }

    fun CoroutineScope.broadcastConflatedLocations(locations: Flow<GpsResult>) =
        locations
            .onEach {
                delay(4000)
                _gpsResults.value = it
            }
            .launchIn(this)
}

/**
 * Credit for singleArgumentViewModelFactory: GoogleCodeLabs
 * https://github.com/googlecodelabs/kotlin-coroutines/blob/master/coroutines-codelab/
 * finished_code/src/main/java/com/example/android/kotlincoroutines/util/ViewModelHelpers.kt
 */
fun <T : ViewModel, A> singleArgumentViewModelFactory(
    constructor: (A) -> T
): (A) -> ViewModelProvider.NewInstanceFactory {
    return { arg: A ->
        object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V {
                return constructor(arg) as V
            }
        }
    }
}