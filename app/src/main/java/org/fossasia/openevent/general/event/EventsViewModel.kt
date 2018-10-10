package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference
import timber.log.Timber

class EventsViewModel(private val eventService: EventService, private val preference: Preference) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val tokenKey = "LOCATION"

    val progress = MutableLiveData<Boolean>()
    val showSchimmerEvents = MutableLiveData<Boolean>()
    val events = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()

    val savedLocation by lazy { preference.getString(tokenKey) }

    fun loadLocationEvents() {
        preference.putString(tokenKey, savedLocation)
        val query = "[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$savedLocation%\"}]"

        compositeDisposable.add(eventService.getEventsByLocation(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally({
                    progress.value = false
                    showSchimmerEvents.value = false
                }).subscribe({
                    events.value = it
                }, {
                    Timber.e(it, "Error fetching events")
                    error.value = "Error fetching events"
                }))
    }

    fun retryLoadLocationEvents() {
        progress.value = true
        loadLocationEvents()
    }

    fun shimmerAndLoadEvents() {
        showSchimmerEvents.value = true
        loadLocationEvents()
    }

    fun loadEvents() {
        compositeDisposable.add(eventService.getEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally({
                    progress.value = false
                }).subscribe({
                    events.value = it
                }, {
                    Timber.e(it, "Error fetching events")
                    error.value = "Error fetching events"
                }))
    }

    fun setFavorite(eventId: Long, favourite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favourite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Success")
                }, {
                    Timber.e(it, "Error")
                    error.value = "Error"
                }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}