package org.fossasia.openevent.general.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SearchEventsDataSource(
    private val eventService: EventService,
    private val compositeDisposable: CompositeDisposable,
    private val query: String,
    private val sortBy: String,
    private val mutableProgress: MutableLiveData<Boolean>
) : PageKeyedDataSource<Int, Event>() {

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Event>) {
        compositeDisposable += eventService.getSearchEventsPaged(query, sortBy, 1)
            .take(1)
            .withDefaultSchedulers()
            .subscribe({ response ->
                if (response.isEmpty())
                    mutableProgress.value = false
                callback.onResult(response, null, 2)
            }, { error ->
                Timber.e(error, "Fail on fetching page of events")
            })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
        compositeDisposable += eventService.getSearchEventsPaged(query, sortBy, params.key)
            .take(1)
            .withDefaultSchedulers()
            .subscribe({ response ->
                if (response.isEmpty())
                    mutableProgress.value = false
                callback.onResult(response, params.key + 1)
            }, { error ->
                Timber.e(error, "Fail on fetching page of events")
            })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
    }
}
