package org.fossasia.openevent.general.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.search.location.SAVED_LOCATION
import org.fossasia.openevent.general.utils.DateTimeUtils
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber
import java.util.Date

class SearchResultsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val resource: Resource,
    private val config: PagedList.Config
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val mutableShowShimmerResults = MediatorLiveData<Boolean>()
    val showShimmerResults: MediatorLiveData<Boolean> = mutableShowShimmerResults
    private val mutablePagedEvents = MutableLiveData<PagedList<Event>>()
    val pagedEvents: LiveData<PagedList<Event>> = mutablePagedEvents
    private val mutableEventTypes = MutableLiveData<List<EventType>>()
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    val eventTypes: LiveData<List<EventType>> = mutableEventTypes
    val connection: LiveData<Boolean> = mutableConnectionLiveData

    private val savedNextDate = DateTimeUtils.getNextDate()
    private val savedNextToNextDate = DateTimeUtils.getNextToNextDate()
    private val savedWeekendDate = DateTimeUtils.getWeekendDate()
    private val savedWeekendNextDate = DateTimeUtils.getNextToWeekendDate()
    private val savedNextMonth = DateTimeUtils.getNextMonth()
    private val savedNextToNextMonth = DateTimeUtils.getNextToNextMonth()
    private lateinit var sourceFactory: SearchEventsDataSourceFactory

    var searchEvent: String? = null
    var savedType: String? = null
    var savedTime: String? = null

    fun loadEventTypes() {
        compositeDisposable += eventService.getEventTypes()
            .withDefaultSchedulers()
            .subscribe({
                mutableEventTypes.value = it
            }, {
                Timber.e(it, "Error fetching events types")
            })
    }

    fun loadEvents(
        location: String,
        time: String,
        type: String,
        freeEvents: Boolean,
        sortBy: String,
        sessionsAndSpeakers: Boolean,
        callForSpeakers: Boolean
    ) {
        if (mutablePagedEvents.value != null) return

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
        val sessionsAndSpeakersFilter = if (sessionsAndSpeakers)
            """, {
               |       'name':'is-sessions-speakers-enabled',
               |       'op':'eq',
               |       'val':'true'
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
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
                |    }$freeStuffFilter
                |    $sessionsAndSpeakersFilter]
                |}]""".trimMargin().replace("'", "\"")
        }
        Timber.e(query)

        sourceFactory = SearchEventsDataSourceFactory(
            compositeDisposable,
            eventService,
            query,
            sortBy,
            mutableShowShimmerResults
        )

        val eventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += eventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
            }.subscribe({
                val currentPagedSearchEvents = mutablePagedEvents.value
                if (currentPagedSearchEvents == null) {
                    mutablePagedEvents.value = it
                } else {
                    currentPagedSearchEvents.addAll(it)
                    mutablePagedEvents.value = currentPagedSearchEvents
                }
            }, {
                Timber.e(it, "Error fetching events")
                mutableError.value = resource.getString(R.string.error_fetching_events_message)
            })
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
        mutablePagedEvents.value = null
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
