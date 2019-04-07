package org.fossasia.openevent.general.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextMonth
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToNextDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToNextMonth
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToWeekendDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getWeekendDate
import timber.log.Timber

class SearchViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val network: Network,
    private val resource: Resource
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
    private val mutableChipClickable = MutableLiveData<Boolean>()
    val chipClickable: LiveData<Boolean> = mutableChipClickable
    var searchEvent: String? = null
    var savedLocation: String? = null
    private val savedNextDate = getNextDate()
    private val savedNextToNextDate = getNextToNextDate()
    private val savedWeekendDate = getWeekendDate()
    private val savedWeekendNextDate = getNextToWeekendDate()
    private val savedNextMonth = getNextMonth()
    private val savedNextToNextMonth = getNextToNextMonth()

    fun loadSavedLocation() {
        savedLocation = preference.getString(SAVED_LOCATION)
    }

    fun loadEvents(location: String, time: String, type: String) {
        if (mutableEvents.value != null) {
            mutableShowShimmerResults.value = false
            mutableShowNoInternetError.value = false
            mutableChipClickable.value = true
        }
        if (!isConnected()) return
        preference.putString(SAVED_LOCATION, location)
        val query: String = when {
            TextUtils.isEmpty(location) -> """[{
                |   'name':'name',
                |   'op':'ilike',
                |   'val':'%$searchEvent%'
                |}]""".trimMargin().replace("'", "'")
            time == "Anytime" && type == "Anything" -> """[{
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
            time == "Anytime" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |    }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |    }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
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
                |   }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
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
                |   }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
                |   }]
                |}]""".trimMargin().replace("'", "\"")
            time == "This weekend" -> """[{
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
                |   }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
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
                |   }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
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
                |   }, {
                |       'name':'event-type',
                |       'op':'has',
                |       'val': {
                |       'name':'name',
                |       'op':'eq',
                |       'val':'$type'
                |       }
                |   }]
                |}]""".trimMargin().replace("'", "\"")
        }

        compositeDisposable.add(eventService.getSearchEvents(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
                mutableChipClickable.value = false
            }.doFinally {
                mutableShowShimmerResults.value = false
                mutableChipClickable.value = true
            }.subscribe({
                mutableEvents.value = it
            }, {
                Timber.e(it, "Error fetching events")
                mutableError.value = resource.getString(R.string.error_fetching_events_message)
            })
        )
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favorite)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Successfully added %d to favorites", eventId)
            }, {
                Timber.e(it, "Error adding %d to favorites", eventId)
                mutableError.value = resource.getString(R.string.error_adding_favorite_message)
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
