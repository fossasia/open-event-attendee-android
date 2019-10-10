package org.fossasia.openevent.general.order

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.util.Date
import kotlin.collections.ArrayList
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.ORDER_STATUS_COMPLETED
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PENDING
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PLACED
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class OrderDataSource(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val compositeDisposable: CompositeDisposable,
    private val userId: Long,
    private val showExpired: Boolean,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val mutableNumOfTickets: MutableLiveData<Int>,
    private val mutableMessage: SingleLiveEvent<String>,
    private val filter: OrderFilter,
    private val fromDb: Boolean
) : PageKeyedDataSource<Int, Pair<Event, Order>>() {
    private val resource = Resource()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Pair<Event, Order>>) {
        if (fromDb)
            getOrdersFromDb(callback)
        else
            createObservable(1, 2, callback, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Pair<Event, Order>>) {
        val page = params.key
        createObservable(page, page + 1, null, callback)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Pair<Event, Order>>) {
        val page = params.key
        createObservable(page, page - 1, null, callback)
    }

    private fun getOrdersFromDb(callback: LoadInitialCallback<Int, Pair<Event, Order>>) {
        compositeDisposable += orderService.getOrderAndEventSourceFactoryFromDb(showExpired)
            .withDefaultSchedulers()
            .subscribe({
                val filteredList = it.filterNotNull().filter {
                    if (filter.isShowingCompletedOrders) true else it.second.status != ORDER_STATUS_COMPLETED
                }.filter {
                    if (filter.isShowingPendingOrders) true else it.second.status != ORDER_STATUS_PENDING
                }.filter {
                    if (filter.isShowingPlacedOrders) true else it.second.status != ORDER_STATUS_PLACED
                }
                if (filteredList.isEmpty()) {
                    createObservable(1, 2, callback, null)
                } else {
                    mutableNumOfTickets.value = filteredList.size
                    callback.onResult(filteredList, null, null)
                    mutableProgress.value = false
                }
            }, {
                mutableMessage.value = resource.getString(R.string.list_events_fail_message)
                Timber.e(it, "Fail on fetching orders ")
                mutableProgress.value = false
            })
    }

    private fun createObservable(
        requestedPage: Int,
        adjacentPage: Int,
        initialCallback: LoadInitialCallback<Int, Pair<Event, Order>>?,
        callback: LoadCallback<Int, Pair<Event, Order>>?
    ) {
        compositeDisposable += getOrdersAndEventsObservable(requestedPage)
            .withDefaultSchedulers()
            .subscribe({
                val response =
                    if (filter.isSortingOrdersByDate)
                        it.sortedByDescending {
                            EventUtils.getTimeInMilliSeconds(it.first.startsAt, null)
                        }
                    else
                        it.sortedBy {
                            it.second.status
                        }

                if (it.isEmpty()) mutableProgress.value = false
                val currentNumOfTicket = mutableNumOfTickets.value ?: 0
                mutableNumOfTickets.value = currentNumOfTicket + response.size
                initialCallback?.onResult(response, null, adjacentPage)
                callback?.onResult(response, adjacentPage)
            }, {
                mutableMessage.value = resource.getString(R.string.list_events_fail_message)
                Timber.e(it, "Fail on fetching orders ")
            })
    }

    private fun getOrdersAndEventsObservable(page: Int): Single<List<Pair<Event, Order>>> {
        val eventIdMap = mutableMapOf<Long, Event>()
        val eventIdAndTimes = mutableMapOf<Long, Int>()
        val operator = if (showExpired) "lt" else "ge"
        val statusList = mutableListOf<String>()
        with(filter) {
            if (isShowingCompletedOrders) statusList.add("'completed'")
            if (isShowingPendingOrders) statusList.add("'pending'")
            if (isShowingPlacedOrders) statusList.add("'placed'")
        }
        val ordersQuery = """[{
                |   'and':[{
                |       'name':'status',
                |       'op':'in',
                |       'val':$statusList
                |    },{
                |       'name':'event',
                |       'op':"has",
                |       'val':{
                |           'name':'deleted-at',
                |           'op':'eq',
                |           'val':null
                |       }
                |    },{
                |       'name':'event',
                |       'op':'has',
                |       'val': {
                |           'name':'ends-at',
                |           'op':'$operator',
                |           'val':'%${EventUtils.getTimeInISO8601(Date())}%'
                |       }
                |    }]
                |}]""".trimMargin().replace("'", "\"")
        val ordersList = orderService.getOrdersOfUserPaged(userId, ordersQuery, page, showExpired)
        return ordersList.flatMap { orders ->
            val ids = orders.map { it.event?.id }.distinct()
            val eventsQuery = """[{
                |   'and':[{
                |       'name':'id',
                |       'op':'in',
                |       'val':$ids
                |    }]
                |}]""".trimMargin().replace("'", "\"")
            eventService.getEventsWithQuery(eventsQuery).map {
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
                val eventAndIdentifier = mutableListOf<Pair<Event, Order>>()
                orders.forEach {
                    val event = eventIdMap[it.event?.id]
                    if (event != null)
                        eventAndIdentifier.add(Pair(event, it))
                }
                eventAndIdentifier as List<Pair<Event, Order>>
            }
        }
    }
}
