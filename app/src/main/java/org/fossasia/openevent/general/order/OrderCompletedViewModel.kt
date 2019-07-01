package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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
    private val mutableSimilarEvents = MutableLiveData<List<Event>>()
    val similarEvents: LiveData<List<Event>> = mutableSimilarEvents

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

    fun fetchSimilarEvents(eventId: Long, topicId: Long, location: String?) {
        if (eventId == -1L) return

        var similarEventsFlowable = eventService.getEventsByLocation(location)

        if (topicId != -1L) {
            similarEventsFlowable = similarEventsFlowable.zipWith(eventService.getSimilarEvents(topicId),
                BiFunction { firstList: List<Event>, secondList: List<Event> ->
                    val similarList = mutableSetOf<Event>()
                    similarList.addAll(firstList + secondList)
                    similarList.toList()
                })
        }

        compositeDisposable += similarEventsFlowable
            .withDefaultSchedulers()
            .distinctUntilChanged()
            .subscribe({ events ->
                val list = events.filter { it.id != eventId }
                mutableSimilarEvents.value = list
            }, {
                Timber.e(it, "Error fetching similar events")
            })
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
