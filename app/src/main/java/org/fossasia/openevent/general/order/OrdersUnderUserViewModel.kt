package org.fossasia.openevent.general.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeService
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.ticket.TicketService
import timber.log.Timber
import kotlin.properties.Delegates.observable

class OrdersUnderUserViewModel(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val attendeeService: AttendeeService,
    private val ticketService: TicketService,
    private val authHolder: AuthHolder,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var order: List<Order>
    private val mutableAttendeesNumber = MutableLiveData<List<Int>>()
    val attendeesNumber: LiveData<List<Int>> = mutableAttendeesNumber
    private var eventIdMap = mutableMapOf<Long, Event>()
    private val eventIdAndTimes = mutableMapOf<Long, Int>()
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEventAndOrderIdentifier = MutableLiveData<List<Pair<Event, String>>>()
    val eventAndOrderIdentifier: LiveData<List<Pair<Event, String>>> =
        mutableEventAndOrderIdentifier
    private val mutableshowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableshowShimmerResults
    private val mutableNoTickets = MutableLiveData<Boolean>()
    val noTickets: LiveData<Boolean> = mutableNoTickets
    private val ticketList = mutableListOf<Ticket>()
    private val attendeeList = mutableListOf<Attendee>()

    fun getId() = authHolder.getId()
    fun isLoggedIn() = authHolder.isLoggedIn()


    class StoreConditions{
        var firstCondition: Boolean by observable(false){ _,_, newValue->
            Log.i("PUI", "first value changed")
            if(newValue && secondCondition){
                storeWork?.invoke()
            }
        }
        var secondCondition: Boolean by observable(false){ _,_, newValue->
            Log.i("PUI", "second value changed")
            if(firstCondition && newValue){
                storeWork?.invoke()
            }
        }

        var storeWork:(()->Unit)? = null
    }

    private var ticketConditionStatus = StoreConditions().apply {
        storeWork = { storeTicketsToLocal()}
    }
    private var attendeeConditionStatus = StoreConditions().apply {
        storeWork = { storeAttendeesLocal() }
    }
    private var orderConditionStatus = StoreConditions().apply {
        storeWork = {storeOrdersLocal()}
    }


    fun orderUnderUserLocal(showExpired: Boolean){
        Log.i("PUI", "order from local")
        compositeDisposable.add(orderService.getOrderLocal()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableshowShimmerResults.value = true
                mutableNoTickets.value = false
            }
            .subscribe({
                order = it
                Log.i("PUI", "order local ${it.size}")
                if(it.isNotEmpty()){
                    mutableAttendeesNumber.value = it.map { it.attendees.size }
                    val eventIds = it.mapNotNull { order -> order.event?.id }
                    if (eventIds.isNotEmpty()) {
                        eventsUnderUserLocal(eventIds, showExpired)
                    } else {
                        mutableshowShimmerResults.value = false
                        mutableNoTickets.value = true
                    }
                }else{
                    ordersUnderUser(showExpired)
                }
            },{
                mutableshowShimmerResults.value = false
                mutableNoTickets.value = true
                mutableMessage.value = resource.getString(R.string.list_orders_fail_message)
                Log.i("PUI", "order local ${it.message}")
            })
        )
    }

    private fun ordersUnderUser(showExpired: Boolean) {
        Log.i("PUI", "order from local")
        compositeDisposable.add(orderService.orderUser(getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableshowShimmerResults.value = true
                mutableNoTickets.value = false
            }.subscribe({
                order = it
                Log.i("PUI", "order online ${it.size}")
                mutableAttendeesNumber.value = it.map { it.attendees.size }

                val eventIds = it.mapNotNull { order -> order.event?.id }
                if (eventIds.isNotEmpty()) {
                    eventsUnderUser(eventIds, showExpired)
                } else {
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


    private fun eventsUnderUserLocal(eventIds: List<Long>, showExpired: Boolean){
        compositeDisposable.add(eventService.getEventWithIds(eventIds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                mutableshowShimmerResults.value = false
            }.subscribe({
                Log.i("PUI", "events local ${it.size}")
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
                Log.i("PUI", "eventList offline ${finalList.size}")
                if (finalList.isEmpty()) mutableNoTickets.value = true
                mutableEventAndOrderIdentifier.value = finalList
            },{

            })
        )
    }

    private fun eventsUnderUser(eventIds: List<Long>, showExpired: Boolean) {
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
                storeTicketDetails(it)
            }, {
                mutableMessage.value = resource.getString(R.string.list_events_fail_message)
                Timber.d(it, "Failed  to list events under a user ")
            })
        )
    }


    private fun getTicketsForEvent(id:Long,isLast:Boolean){
        Log.i("PUI","gettingTickets")
        compositeDisposable.add(
            ticketService.getTicketsForEvent(id).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("PUI", "tickets fetched ${it.size}")
                    ticketList.addAll(it)
                    if (isLast)
                        ticketConditionStatus.secondCondition = true
                },{

                })
        )
    }

    private fun getAttendeesUnderOrder(identifier:String, isLast: Boolean){
        Log.i("PUI","gettingAttendees")
        compositeDisposable.add(
            attendeeService.getAttendeesUnderOrder(identifier)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        attendeeList.addAll(it)
                        if (isLast)
                            attendeeConditionStatus.secondCondition = true
                    },{

                })
        )
    }


    private fun storeTicketDetails(events: List<Event>){
        storeEventsToLocal(events)
        val lastEvent = events.last()
        for (event in events){
            getTicketsForEvent(event.id, event == lastEvent)
        }

        val lastOrder = order.last()
        for(order in order){
            order.identifier?.let {
                getAttendeesUnderOrder(it, order == lastOrder)
            }
        }
    }

    private fun storeEventsToLocal(events:List<Event>){
        compositeDisposable.add(
            Completable.fromAction {eventService.storeEventToLocal(events)}
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("PUI", "event stored")
                    ticketConditionStatus.firstCondition = true
                },{

                })
        )
    }


    private fun storeTicketsToLocal(){
        Log.i("PUI", "Tickets list ${ticketList.size}")
        compositeDisposable.add(
            Completable.fromAction {ticketService.storeTicketsToLocal(ticketList)}
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("PUI", "ticket stored")
                    attendeeConditionStatus.firstCondition = true
                },{
                    Log.i("PUI", "ticket error ${it.message}")
                })
        )
    }

    private fun storeAttendeesLocal(){
        Log.i("PUI", "storing attendees ${attendeeList.size}")
        val attendees = attendeeList.map {
            when {
                it.ticket == null && it.ticketId != null
                -> it.copy(ticket = TicketId(it.ticketId.toLong()))
                else -> it.copy()
            }
        }

        compositeDisposable.add(
            Completable.fromAction {attendeeService.storeAttendees(attendees)}
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("PUI", "attendee stored")
                    orderConditionStatus.apply {
                        firstCondition = true
                        secondCondition = true
                    }
                },{
                    Log.i("PUI", "attendee error ${it.message}")
                })
        )
    }

    private fun storeOrdersLocal(){
        compositeDisposable.add(
            Completable.fromAction {orderService.storeOrderLocal(order)}
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("PUI", "order stored")
                },{
                    Log.i("PUI", "order error ${it.message}")
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
