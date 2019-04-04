package org.fossasia.openevent.general.order

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

class OrderCompletedViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            throw IllegalStateException("ID should never be -1")
        }

        compositeDisposable.add(
            eventService.getEvent(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mutableEvent.value = it
                }, {
                    Timber.e(it, "Error fetching event %d", id)
                    mutableMessage.value = resource.getString(R.string.error_fetching_event_message)
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
