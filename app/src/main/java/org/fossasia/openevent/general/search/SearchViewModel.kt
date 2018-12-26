package org.fossasia.openevent.general.search

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    private val tokenKey = "LOCATION"

    val showShimmerResults = MutableLiveData<Boolean>()
    val events = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()
    val showNoInternetError = MutableLiveData<Boolean>()
    var searchEvent: String? = null
    val savedLocation by lazy { preference.getString(tokenKey) }
    val savedDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyDate) }
    private val savedNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextDate) }
    private val savedNextToNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextToNextDate) }
    private val savedWeekendDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyWeekendDate) }
    private val savedWeekendNextDate by lazy { preference.getString(SearchTimeViewModel.tokenKeyWeekendNextDate) }
    private val savedNextMonth by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextMonth) }
    private val savedNextToNextMonth by lazy { preference.getString(SearchTimeViewModel.tokenKeyNextToNextMonth) }

    fun loadEvents(location: String, time: String) {
        if (!isConnected()) return
        preference.putString(tokenKey, location)
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
                |       'val':'$savedDate%'
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
                |       'val':'$savedDate%'
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
                    showShimmerResults.value = true
                }.doFinally {
                    showShimmerResults.value = false
                }.subscribe({
                    events.value = it
                }, {
                    Timber.e(it, "Error fetching events")
                    error.value = "Error fetching events"
                }))

        preference.remove(SearchTimeViewModel.tokenKeyDate)
        preference.remove(SearchTimeViewModel.tokenKeyNextDate)
    }

    fun setFavorite(eventId: Long, favourite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favourite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Successfully added %d to favorites", eventId)
                }, {
                    Timber.e(it, "Error adding %d to favorites", eventId)
                    error.value = "Error adding to favorites"
                }))
    }

    fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        showNoInternetError.value = !isConnected
        showShimmerResults.value = isConnected
        return isConnected
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
