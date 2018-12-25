package org.fossasia.openevent.general.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class AboutEventViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progressAboutEvent = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()
    val error = MutableLiveData<String>()

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            error.value = "Error fetching event"
            return
        }
        compositeDisposable.add(eventService.getEvent(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progressAboutEvent.value = true
                }).doFinally({
                    progressAboutEvent.value = false
                }).subscribe({ eventList ->
                    event.value = eventList
                }, {
                    error.value = "Error fetching event"
                    Timber.e(it, "Error fetching event %d", id)
                }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
