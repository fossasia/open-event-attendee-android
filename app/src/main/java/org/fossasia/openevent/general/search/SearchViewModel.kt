package org.fossasia.openevent.general.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextMonth
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToNextDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToNextMonth
import org.fossasia.openevent.general.utils.DateTimeUtils.getNextToWeekendDate
import org.fossasia.openevent.general.utils.DateTimeUtils.getWeekendDate
import timber.log.Timber
import java.util.Date

class SearchViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableShowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableShowShimmerResults
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    val connection: LiveData<Boolean> = mutableConnectionLiveData
    var searchEvent: String? = null
    var savedLocation: String? = null
    var savedType: String? = null
    var savedTime: String? = null
    private val savedNextDate = getNextDate()
    private val savedNextToNextDate = getNextToNextDate()
    private val savedWeekendDate = getWeekendDate()
    private val savedWeekendNextDate = getNextToWeekendDate()
    private val savedNextMonth = getNextMonth()
    private val savedNextToNextMonth = getNextToNextMonth()
    private val mutableEventTypes = MutableLiveData<List<EventType>>()
    val eventTypes: LiveData<List<EventType>> = mutableEventTypes
    var searchViewQuery: String = ""
    var isQuerying = false

    fun loadEventTypes() {
        compositeDisposable += eventService.getEventTypes()
            .withDefaultSchedulers()
            .subscribe({
                mutableEventTypes.value = it
            }, {
                Timber.e(it, "Error fetching events types")
            })
    }

    fun loadSavedLocation() {
        savedLocation = preference.getString(SAVED_LOCATION) ?: resource.getString(R.string.choose_your_location)
    }
    fun loadSavedType() {
        savedType = preference.getString(SAVED_TYPE)
    }
    fun loadSavedTime() {
        savedTime = preference.getString(SAVED_TIME)
    }

    fun loadEvents(location: String, time: String, type: String, freeEvents: Boolean, sortBy: String) {
        if (mutableEvents.value != null) {
            return
        }
        if (!isConnected()) return
        preference.putString(SAVED_LOCATION, location)

        val freeStuffFilter = if (freeEvents)
            """, {
               |    'name':'tickets',
               |    'op':'any',
               |    'val':{
               |        'name':'price',
               |        'op':'eq',
               |        'val':'0'
               |    }
               |}, {
               |       'name':'ends-at',
               |       'op':'ge',
               |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
               |    }
            """.trimIndent()
            else ""
        val query: String = when {
            TextUtils.isEmpty(location) -> """[{
                |   'name':'name',
                |   'op':'ilike',
                |   'val':'%$searchEvent%'
                |}, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }]""".trimMargin().replace("'", "'")
            time == "Anytime" && type == "Anything" -> """[{
                |   'and':[{
                |       'name':'location-name',
                |       'op':'ilike',
                |       'val':'%$location%'
                |    }, {
                |       'name':'name',
                |       'op':'ilike',
                |       'val':'%$searchEvent%'
                |    }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |    }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |   }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |   }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |   }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |   }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
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
                |   }, {
                |       'name':'ends-at',
                |       'op':'ge',
                |       'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |    }$freeStuffFilter]
                |}]""".trimMargin().replace("'", "\"")
        }
        compositeDisposable += eventService.getSearchEvents(query, sortBy)
            .withDefaultSchedulers()
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
            }.doFinally {
                stopLoaders()
            }.subscribe({
                stopLoaders()
                mutableEvents.value = it
            }, {
                stopLoaders()
                Timber.e(it, "Error fetching events")
                mutableError.value = resource.getString(R.string.error_fetching_events_message)
            })
    }

    private fun stopLoaders() {
        mutableShowShimmerResults.value = false
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Successfully added %d to favorites", eventId)
            }, {
                Timber.e(it, "Error adding %d to favorites", eventId)
                mutableError.value = resource.getString(R.string.error_adding_favorite_message)
            })
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        mutableEvents.value = null
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
