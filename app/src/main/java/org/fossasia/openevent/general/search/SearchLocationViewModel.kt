package org.fossasia.openevent.general.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.location.EventLocation
import timber.log.Timber

const val SAVED_LOCATION = "LOCATION"

class SearchLocationViewModel(
    private val eventService: EventService,
    private val preference: Preference
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventLocations = MutableLiveData<List<EventLocation>>()
    val eventLocations: LiveData<List<EventLocation>> = mutableEventLocations
    private val mutableShowShimmer = MutableLiveData<Boolean>()
    val showShimmer: LiveData<Boolean> = mutableShowShimmer

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)
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
