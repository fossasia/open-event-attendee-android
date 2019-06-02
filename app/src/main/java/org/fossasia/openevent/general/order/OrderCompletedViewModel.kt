package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
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
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSimilarEvents = MutableLiveData<Set<Event>>()
    val similarEvents: LiveData<Set<Event>> = mutableSimilarEvents

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            throw IllegalStateException("ID should never be -1")
        }

        compositeDisposable +=
            eventService.getEvent(id)
                .withDefaultSchedulers()
                .subscribe({
                    mutableEvent.value = it
                }, {
                    Timber.e(it, "Error fetching event %d", id)
                    mutableMessage.value = resource.getString(R.string.error_fetching_event_message)
                })
    }

    fun fetchSimilarEvents(eventId: Long, topicId: Long) {
        if (eventId == -1L) return

        if (topicId != -1L) {
            compositeDisposable += eventService.getSimilarEvents(topicId)
                .withDefaultSchedulers()
                .doOnSubscribe {
                    mutableProgress.value = true
                }.subscribe({ events ->
                    val list = events.filter { it.id != eventId }
                    val oldList = mutableSimilarEvents.value

                    val similarEventList = mutableSetOf<Event>()
                    similarEventList.addAll(list)
                    oldList?.let {
                        similarEventList.addAll(it)
                    }
                    mutableProgress.value = false
                    mutableSimilarEvents.value = similarEventList
                }, {
                    Timber.e(it, "Error fetching similar events")
                })
        }
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
