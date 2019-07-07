package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.paging.SimilarEventsDataSourceFactory
import timber.log.Timber

class OrderCompletedViewModel(
    private val eventService: EventService,
    private val resource: Resource,
    private val config: PagedList.Config
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSimilarEvents = MediatorLiveData<PagedList<Event>>()
    val similarEvents: MediatorLiveData<PagedList<Event>> = mutableSimilarEvents

    fun loadEvent(id: Long) {
        if (id == -1L) {
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

        val sourceFactory = SimilarEventsDataSourceFactory(
            compositeDisposable,
            topicId,
            location,
            eventId,
            mutableProgress,
            eventService
        )

        val similarEventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += similarEventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({ events ->
                val currentPagedSimilarEvents = mutableSimilarEvents.value
                if (currentPagedSimilarEvents == null) {
                    mutableSimilarEvents.value = events
                } else {
                    currentPagedSimilarEvents.addAll(events)
                    mutableSimilarEvents.value = currentPagedSimilarEvents
                }
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
