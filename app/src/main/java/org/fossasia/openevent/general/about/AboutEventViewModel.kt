package org.fossasia.openevent.general.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class AboutEventViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgressAboutEvent = MutableLiveData<Boolean>()
    val progressAboutEvent: LiveData<Boolean> = mutableProgressAboutEvent
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            mutableError.value = Resource().getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable.add(eventService.getEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgressAboutEvent.value = true
            }.doFinally {
                mutableProgressAboutEvent.value = false
            }.subscribe({ eventList ->
                mutableEvent.value = eventList
            }, {
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
                Timber.e(it, "Error fetching event %d", id)
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
