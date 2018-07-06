package org.fossasia.openevent.general.event.topic

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class SimilarEventsViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val similarEvents = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()
    var eventId: Long = -1

    fun loadSimilarEvents(id: Long) {
        if(id == -1L){
            return
        }
        compositeDisposable.add(eventService.getSimilarEvents(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).subscribe({
                    progress.value = false
                    similarEvents.value = it.filter { it.id != eventId }
                }, {
                    Timber.e(it, "Error fetching similar events")
                    error.value = "Error fetching similar events"
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