package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.search.SAVED_LOCATION
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class EventsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MediatorLiveData<Boolean>()
    val progress: MediatorLiveData<Boolean> = mutableProgress
    private lateinit var mutableEventsPaged: Observable<PagedList<Event>>
    private val mutableEvents = MutableLiveData<PagedList<Event>>()
    val events: LiveData<PagedList<Event>> = mutableEvents
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableShowShimmerEvents = MutableLiveData<Boolean>()
    val showShimmerEvents: LiveData<Boolean> = mutableShowShimmerEvents
    var lastSearch = ""
    private val mutableSavedLocation = MutableLiveData<String>()
    val savedLocation: LiveData<String> = mutableSavedLocation
    private lateinit var sourceFactory: EventsDataSourceFactory

    init {
        progress.addSource(mutableProgress) {
            if (!it) {
               stopLoaders()
                progress.removeSource(mutableProgress)
            }
        }
    }
    fun loadLocation() {
        mutableSavedLocation.value = preference.getString(SAVED_LOCATION)
    }

    fun loadLocationEvents() {
        if (lastSearch != savedLocation.value) {
            sourceFactory = EventsDataSourceFactory(
                compositeDisposable,
                eventService,
                mutableSavedLocation.value,
                mutableProgress
            )
            mutableEventsPaged = RxPagedListBuilder(sourceFactory, config)
                .setFetchScheduler(Schedulers.io())
                .buildObservable()
                .cache()

            compositeDisposable += mutableEventsPaged
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .doOnSubscribe {
                    mutableShowShimmerEvents.value = true
                }
                .subscribe({
                    mutableEvents.value = it
                }, {
                    stopLoaders()
                    Timber.e(it, "Error fetching events")
                    mutableError.value = resource.getString(R.string.error_fetching_events_message)
                })
        } else {
            mutableProgress.value = false
        }
    }

    private fun stopLoaders() {
        mutableProgress.value = false
        mutableShowShimmerEvents.value = false
        lastSearch = mutableSavedLocation.value ?: ""
    }
    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        compositeDisposable.clear()
    }

    fun clearLastSearch() {
        lastSearch = ""
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
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
