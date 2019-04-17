package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import timber.log.Timber

class OrdersUnderUserViewModel(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var order: List<Order>
    private val mutableAttendeesNumber = MutableLiveData<List<Int>>()
    val attendeesNumber: LiveData<List<Int>> = mutableAttendeesNumber
    private var eventIdMap = mutableMapOf<Long, Event>()
    private val eventIdAndTimes = mutableMapOf<Long, Int>()
    private var eventId: Long = -1
    private val idList = ArrayList<Long>()
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEventAndOrderIdentifier = MutableLiveData<List<Pair<Event, String>>>()
    val eventAndOrderIdentifier: LiveData<List<Pair<Event, String>>> =
        mutableEventAndOrderIdentifier
    private val mutableshowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableshowShimmerResults
    private val mutableNoTickets = MutableLiveData<Boolean>()
    val noTickets: LiveData<Boolean> = mutableNoTickets

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun ordersUnderUser(showExpired: Boolean) {
        compositeDisposable.add(orderService.orderUser(getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableshowShimmerResults.value = true
                mutableNoTickets.value = false
            }.subscribe({
                order = it
                mutableAttendeesNumber.value = it.map { it.attendees.size }
                val query = buildQuery(it)

                if (idList.size != 0)
                    eventsUnderUser(query, showExpired)
                else {
                    mutableshowShimmerResults.value = false
                    mutableNoTickets.value = true
                }
            }, {
                mutableshowShimmerResults.value = false
                mutableNoTickets.value = true
                mutableMessage.value = resource.getString(R.string.list_orders_fail_message)
                Timber.d(it, "Failed  to list Orders under a user ")
            })
        )
    }

    private fun eventsUnderUser(eventIds: String, showExpired: Boolean) {
        compositeDisposable.add(eventService.getEventsUnderUser(eventIds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                mutableshowShimmerResults.value = false
            }.subscribe({
                val events = ArrayList<Event>()
                it.map {
                    val times = eventIdAndTimes[it.id]
                    if (times != null) {
                        for (i in 0..times) {
                            events.add(it)
                        }
                    }
                    eventIdMap[it.id] = it
                }
                var eventAndIdentifier = ArrayList<Pair<Event, String>>()
                var finalList: List<Pair<Event, String>>
                order.forEach {
                    val event = eventIdMap[it.event?.id]
                    if (event != null && it.identifier != null)
                        eventAndIdentifier.add(Pair(event, it.identifier))
                }
                finalList = eventAndIdentifier
                when (showExpired) {
                    false -> finalList = finalList.filter {
                        EventUtils.getTimeInMilliSeconds(it.first.endsAt, null) > System.currentTimeMillis() }
                    true -> finalList = finalList.filter {
                        EventUtils.getTimeInMilliSeconds(it.first.endsAt, null) < System.currentTimeMillis() }
                }
                if (finalList.isEmpty()) mutableNoTickets.value = true
                mutableEventAndOrderIdentifier.value = finalList
            }, {
                mutableMessage.value = resource.getString(R.string.list_events_fail_message)
                Timber.d(it, "Failed  to list events under a user ")
            })
        )
    }

    private fun buildQuery(orderList: List<Order>): String {
        var subQuery = ""

        eventIdAndTimes.clear()
        orderList.forEach {
            it.event?.id?.let { it1 ->
                val times = eventIdAndTimes[it1]
                if (eventIdAndTimes.containsKey(it1) && times != null) {
                    eventIdAndTimes[it1] = times + 1
                } else {
                    eventIdAndTimes[it1] = 1
                }
                idList.add(it1)
                eventId = it1
                subQuery += ",{\"name\":\"id\",\"op\":\"eq\",\"val\":\"$eventId\"}"
            }
        }

        val formattedSubQuery = if (subQuery != "")
            subQuery.substring(1) // remove "," from the beginning
        else
            "" // if there are no orders

        return if (idList.size == 1)
            "[{\"name\":\"id\",\"op\":\"eq\",\"val\":\"$eventId\"}]"
        else
            "[{\"or\":[$formattedSubQuery]}]"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
