package org.fossasia.openevent.general.event.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class EventsDataSource(
    private val eventService: EventService,
    private val compositeDisposable: CompositeDisposable,
    private val query: String?,
    private val mutableProgress: MutableLiveData<Boolean>

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
        compositeDisposable +=
            eventService.getEventsByLocationPaged(query, requestedPage)
                .withDefaultSchedulers()
                .subscribe({ response ->
                    if (response.isEmpty()) mutableProgress.value = false
                    initialCallback?.onResult(response, null, adjacentPage)
                    callback?.onResult(response, adjacentPage)
                }, { error ->
                    Timber.e(error, "Fail on fetching page of events")
                }
            )
    }
}
