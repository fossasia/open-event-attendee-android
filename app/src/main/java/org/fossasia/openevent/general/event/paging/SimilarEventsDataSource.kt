package org.fossasia.openevent.general.event.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SimilarEventsDataSource(
    private val compositeDisposable: CompositeDisposable,
    private val topicId: Long,
    private val location: String?,
    private val eventId: Long,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val eventService: EventService
) : PageKeyedDataSource<Int, Event>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Event>
    ) {
        createObservable(1, 2, callback, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
        val page = params.key
        createObservable(page, page + 1, null, callback)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
        val page = params.key
        createObservable(page, page - 1, null, callback)
    }

    private fun createObservable(
        requestedPage: Int,
        adjacentPage: Int,
        initialCallback: LoadInitialCallback<Int, Event>?,
        callback: LoadCallback<Int, Event>?
    ) {
        var similarEventsFlowable = eventService.getEventsByLocationPaged(location, requestedPage, 3)
        if (topicId != -1L) {
            similarEventsFlowable = similarEventsFlowable
                .zipWith(eventService.getSimilarEventsPaged(topicId, requestedPage, 3),
                    BiFunction { firstList: List<Event>, secondList: List<Event> ->
                        val similarList = mutableSetOf<Event>()
                        similarList.addAll(firstList + secondList)
                        similarList.toList()
                    })
        }

        compositeDisposable += similarEventsFlowable
            .take(1)
            .withDefaultSchedulers()
            .subscribe({ response ->
                if (response.isEmpty()) {
                    mutableProgress.value = false
                }
                initialCallback?.onResult(response.filter { it.id != eventId }, null, adjacentPage)
                callback?.onResult(response, adjacentPage)
            }, { error ->
                Timber.e(error, "Fail on fetching page of events")
            })
    }
}
