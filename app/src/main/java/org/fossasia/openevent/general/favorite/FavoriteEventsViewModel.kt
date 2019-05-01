package org.fossasia.openevent.general.favorite

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

class FavoriteEventsViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents

    fun loadFavoriteEvents() {
        compositeDisposable +=
            eventService.getFavoriteEvents()
                .withDefaultSchedulers()
                .subscribe({
                    mutableEvents.value = it
                    mutableProgress.value = false
                }, {
                    Timber.e(it, "Error fetching favorite events")
                    mutableError.value = resource.getString(R.string.fetch_favorite_events_error_message)
                })
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable +=
            eventService.setFavorite(eventId, favorite)
                .withDefaultSchedulers()
                .subscribe({
                    Timber.d("Success")
                }, {
                    Timber.e(it, "Error")
                    mutableError.value = resource.getString(R.string.error)
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
