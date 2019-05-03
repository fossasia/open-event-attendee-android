package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.search.SAVED_LOCATION
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class EventsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableShowShimmerEvents = MutableLiveData<Boolean>()
    val showShimmerEvents: LiveData<Boolean> = mutableShowShimmerEvents
    var lastSearch = ""
    private val mutableSavedLocation = MutableLiveData<String>()
    val savedLocation: LiveData<String> = mutableSavedLocation

    fun loadLocation() {
        mutableSavedLocation.value = preference.getString(SAVED_LOCATION)
    }

    fun loadLocationEvents() {
        if (lastSearch != savedLocation.value) {
            compositeDisposable += eventService.getEventsByLocation(mutableSavedLocation.value)
                .withDefaultSchedulers()
                .distinctUntilChanged()
                .doOnSubscribe {
                    mutableShowShimmerEvents.value = true
                }
                .doFinally {
                    stopLoaders()
                }.subscribe({
                    stopLoaders()
                    mutableEvents.value = it
                }, {
                    stopLoaders()
                    Timber.e(it, "Error fetching events")
                    mutableError.value = resource.getString(R.string.error_fetching_events_message)
                })
        } else {
            mutableProgress.value = false
        }
    }

    private fun stopLoaders() {
        mutableProgress.value = false
        mutableShowShimmerEvents.value = false
        lastSearch = mutableSavedLocation.value ?: ""
    }
    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        mutableEvents.value = null
    }

    fun clearLastSearch() {
        lastSearch = ""
    }

    fun loadEvents() {
        compositeDisposable += eventService.getEvents()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableEvents.value = it
            }, {
                Timber.e(it, "Error fetching events")
                mutableError.value = resource.getString(R.string.error_fetching_events_message)
            })
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutableError.value = resource.getString(R.string.error)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
