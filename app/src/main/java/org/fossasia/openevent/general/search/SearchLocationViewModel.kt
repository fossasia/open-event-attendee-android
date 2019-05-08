package org.fossasia.openevent.general.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.BuildConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.location.EventLocation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val SAVED_LOCATION = "LOCATION"
const val SEARCH_INTERVAL = 250L

class SearchLocationViewModel(
    private val eventService: EventService,
    private val preference: Preference
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventLocations = MutableLiveData<List<EventLocation>>()
    val eventLocations: LiveData<List<EventLocation>> = mutableEventLocations
    private val mutableShowShimmer = MutableLiveData<Boolean>()
    val showShimmer: LiveData<Boolean> = mutableShowShimmer
    private var geoCodingRequest: MapboxGeocoding? = null

    val placeSuggestions = MutableLiveData<List<CarmenFeature>>()

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)
    }

    private fun loadPlaceSuggestions(query: String) {
        // Cancel Previous Call
        geoCodingRequest?.cancelCall()
        doAsync {
            geoCodingRequest = makeGeocodingRequest(query)
            val list = geoCodingRequest?.executeCall()?.body()?.features()
            uiThread { placeSuggestions.value = list }
        }
    }

    private fun makeGeocodingRequest(query: String) = MapboxGeocoding.builder()
        .accessToken(BuildConfig.MAPBOX_KEY)
        .query(query)
        .languages("en")
        .build()

    fun handlePlaceSuggestions(observableQuery: Observable<String>) {
        compositeDisposable += (
            observableQuery.debounce(SEARCH_INTERVAL, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadPlaceSuggestions(it)
                }
        )
    }

    fun loadEventsLocation() {
        compositeDisposable += eventService.getEventLocations()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableShowShimmer.value = true
            }
            .doFinally {
                mutableShowShimmer.value = false
            }
            .subscribe({
                mutableEventLocations.value = it
            }, {
                Timber.e(it, "Error fetching events")
            })
    }

    override fun onCleared() {
        super.onCleared()
        geoCodingRequest?.cancelCall()
        compositeDisposable.dispose()
    }
}
