package org.fossasia.openevent.general.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class SearchViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val events = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()
    var searchEvent: String? = null

    fun loadEvents() {
        val query = "[{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"}]"

        compositeDisposable.add(eventService.getSearchEvents(query)
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