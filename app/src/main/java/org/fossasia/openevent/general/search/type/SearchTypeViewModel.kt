package org.fossasia.openevent.general.search.type

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.types.EventType
import timber.log.Timber

const val SAVED_TYPE = "TYPE"

class SearchTypeViewModel(
    private val preference: Preference,
    private val eventService: EventService,
    private val connectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventTypes = MutableLiveData<List<EventType>>()
    val eventTypes: LiveData<List<EventType>> = mutableEventTypes
    val connection: LiveData<Boolean> = connectionLiveData
    private val mutableShowShimmer = MutableLiveData<Boolean>()
    val showShimmer: LiveData<Boolean> = mutableShowShimmer

    fun loadEventTypes() {
        compositeDisposable += eventService.getEventTypes()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableShowShimmer.value = true
            }
            .doFinally {
                mutableShowShimmer.value = false
            }
            .subscribe({
                mutableEventTypes.value = it
            }, {
                Timber.e(it, "Error fetching events types")
            })
    }

    fun saveType(query: String) {
        preference.putString(SAVED_TYPE, query)
    }

    fun isConnected(): Boolean = connectionLiveData.value ?: false

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
