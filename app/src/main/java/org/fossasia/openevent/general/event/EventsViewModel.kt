package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EventsViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val events = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()

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

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}