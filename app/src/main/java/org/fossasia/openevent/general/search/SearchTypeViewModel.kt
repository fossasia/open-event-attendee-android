package org.fossasia.openevent.general.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.types.EventType
import timber.log.Timber

const val SAVED_TYPE = "TYPE"

class SearchTypeViewModel(
    private val preference: Preference,
    private val eventService: EventService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventTypes = MutableLiveData<List<EventType>>()
    val eventTypes: LiveData<List<EventType>> = mutableEventTypes

    fun loadEventTypes() {
        compositeDisposable.add(eventService.getEventTypes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableEventTypes.value = it
            }, {
                Timber.e(it, "Error fetching events types")
            })
        )
    }

    fun saveType(query: String) {
        preference.putString(SAVED_TYPE, query)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
