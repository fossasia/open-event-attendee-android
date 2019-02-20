package org.fossasia.openevent.general.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class SearchViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val network: Network
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableShowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableShowShimmerResults
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableShowNoInternetError = MutableLiveData<Boolean>()
    val showNoInternetError: LiveData<Boolean> = mutableShowNoInternetError
    var searchEvent: String? = null
    var savedLocation: String? = null
    private val savedNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextDate) }
    private val savedNextToNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextToNextDate) }
    private val savedWeekendDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyWeekendDate) }
    private val savedWeekendNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyWeekendNextDate) }
    private val savedNextMonth by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextMonth) }
    private val savedNextToNextMonth by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextToNextMonth) }

    fun loadSavedLocation() {
        savedLocation = preference.getString(SAVED_LOCATION)
    }

    fun loadEvents(location: String, time: String) {
        if (!isConnected()) return
        preference.putString(SAVED_LOCATION, location)
        val query: String = when {
            TextUtils.isEmpty(location) -> """[{
                |   'name':'name',
                |   'op':'ilike',
                |   'val':'%$searchEvent%'
                |}]""".trimMargin().replace("'", "'")
            time == "Anytime" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |    }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |    }]
                |}]""".trimMargin().replace("'", "\"")
            time == "Today" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |   }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'ge',
                |       'val':'$time%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'lt',
                |       'val':'$savedNextDate%'
                |   }]
                |}]""".trimMargin().replace("'", "\"")
            time == "Tomorrow" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |   }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'ge',
                |       'val':'$savedNextDate%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'lt',
                |       'val':'$savedNextToNextDate%'
                |   }]
                |}]""".trimMargin().replace("'", "\"")
            time == "This Weekend" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |   }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'ge',
                |       'val':'$savedWeekendDate%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'lt',
                |       'val':'$savedWeekendNextDate%'
                |   }]
                |}]""".trimMargin().replace("'", "\"")
            time == "In the next month" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |   }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'ge',
                |       'val':'$savedNextMonth%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'lt',
                |       'val':'$savedNextToNextMonth%'
                |   }]
                |}]""".trimMargin().replace("'", "\"")
            else -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |   }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'ge',
                |       'val':'$time%'
                |   }, {
                |       'name':'starts-at',
                |       'op':'lt',
                |       'val':'$savedNextDate%'
                |   }]
                |}]""".trimMargin().replace("'", "\"")
        }

        compositeDisposable.add(eventService.getSearchEvents(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
            }.doFinally {
                mutableShowShimmerResults.value = false
            }.subscribe({
                mutableEvents.value = it
            }, {
                Timber.e(it, "Error fetching events")
                mutableError.value = "Error fetching events"
            })
        )
        preference.remove(SearchTimeViewModel.tokenKeyNextDate)
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favorite)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Successfully added %d to favorites", eventId)
            }, {
                Timber.e(it, "Error adding %d to favorites", eventId)
                mutableError.value = "Error adding to favorites"
            })
        )
    }

    fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        mutableShowNoInternetError.value = !isConnected
        mutableShowShimmerResults.value = isConnected
        return isConnected
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
