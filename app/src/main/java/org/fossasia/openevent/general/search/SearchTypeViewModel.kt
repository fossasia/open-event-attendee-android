package org.fossasia.openevent.general.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.types.EventType
import timber.log.Timber

class SearchTypeViewModel(
    private val eventService: EventService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventLocations = MutableLiveData<List<EventType>>()
    val eventLocations: LiveData<List<EventType>> = mutableEventLocations

    fun loadEventTypes() {
        compositeDisposable.add(eventService.getEventTypes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableEventLocations.value = it
            }, {
                Timber.e(it, "Error fetching events types")
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
