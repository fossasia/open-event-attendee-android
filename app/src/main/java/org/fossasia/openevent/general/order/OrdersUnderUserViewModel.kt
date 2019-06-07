package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
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
    private var eventIdMap = mutableMapOf<Long, Event>()
    private val eventIdAndTimes = mutableMapOf<Long, Int>()
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEventAndOrder = MutableLiveData<List<Pair<Event, Order>>>()
    val eventAndOrder: LiveData<List<Pair<Event, Order>>> = mutableEventAndOrder
    private val mutableShowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableShowShimmerResults
    private val mutableNoTickets = MutableLiveData<Boolean>()
    val noTickets: LiveData<Boolean> = mutableNoTickets

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun ordersUnderUser(showExpired: Boolean) {
        compositeDisposable += orderService.getOrdersOfUser(getId())
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
                mutableNoTickets.value = false
            }.subscribe({
                order = it
                val eventIds = it.mapNotNull { order -> order.event?.id }
                if (eventIds.isNotEmpty()) {
                    eventsUnderUser(eventIds, showExpired)
                } else {
                    mutableShowShimmerResults.value = false
                    mutableNoTickets.value = true
                }
            }, {
                mutableShowShimmerResults.value = false
                mutableNoTickets.value = true
                mutableMessage.value = resource.getString(R.string.list_orders_fail_message)
                Timber.d(it, "Failed  to list Orders under a user ")
            })
    }

    private fun eventsUnderUser(eventIds: List<Long>, showExpired: Boolean) {
        compositeDisposable += eventService.getEventsUnderUser(eventIds)
            .withDefaultSchedulers()
            .distinctUntilChanged()
            .doFinally {
                mutableShowShimmerResults.value = false
            }.subscribe({
                mutableShowShimmerResults.value = false
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
                val eventAndIdentifier = ArrayList<Pair<Event, Order>>()
                order.forEach {
                    val event = eventIdMap[it.event?.id]
                    if (event != null)
                        eventAndIdentifier.add(Pair(event, it))
                }
                val finalList = when (showExpired) {
                    false -> eventAndIdentifier.filter {
                        EventUtils.getTimeInMilliSeconds(it.first.endsAt, null) > System.currentTimeMillis() }
                    true -> eventAndIdentifier.filter {
                        EventUtils.getTimeInMilliSeconds(it.first.endsAt, null) < System.currentTimeMillis() }
                }
                if (finalList.isEmpty()) mutableNoTickets.value = true
                mutableEventAndOrder.value = finalList
            }, {
                mutableShowShimmerResults.value = false
                mutableMessage.value = resource.getString(R.string.list_events_fail_message)
                Timber.d(it, "Failed  to list events under a user ")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
