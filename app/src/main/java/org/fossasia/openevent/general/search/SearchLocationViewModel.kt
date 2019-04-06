package org.fossasia.openevent.general.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import io.reactivex.Observable
import org.fossasia.openevent.general.BuildConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.location.EventLocation
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

    val placeSuggestions = MutableLiveData<List<CarmenFeature>>()

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)
    }

    private fun loadPlaceSuggestions(query: String) {
        val geoCodingRequest = makeGeocodingRequest(query)

        geoCodingRequest.enqueueCall(object : Callback<GeocodingResponse> {
            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {}

            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                val results = response.body()?.features()
                placeSuggestions.value = results
            }
        })
    }

    private fun makeGeocodingRequest(query: String) = MapboxGeocoding.builder()
        .accessToken(BuildConfig.MAPBOX_KEY)
        .query(query)
        .build()

    fun handlePlaceSuggesstions(observableQuery: Observable<String>) {
        compositeDisposable.add(
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
        compositeDisposable.add(eventService.getEventLocations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
