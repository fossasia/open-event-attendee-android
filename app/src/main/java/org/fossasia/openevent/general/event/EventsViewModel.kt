package org.fossasia.openevent.general.event

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class EventsViewModel(private val eventService: EventService, val factory: EventDataSourceFactory) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val events: LiveData<PagedList<Event>>
    val error = MutableLiveData<String>()

    init {

        val config = PagedList.Config.Builder().setPageSize(10).setInitialLoadSizeHint(20).setPrefetchDistance(2).build()
        events = LivePagedListBuilder(factory, config).build()
    }

    fun swipeRefresh() {
        factory.eventDataSourceLiveData.getValue()?.invalidate()
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