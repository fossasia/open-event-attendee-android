package org.fossasia.openevent.general.order

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class OrdersUnderUserVM(private val orderService: OrderService, private val eventService: EventService, private val authHolder: AuthHolder) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var order: List<Order>
    private var eventIdMap = mutableMapOf<Long, Event>()
    private val eventIdAndTimes = mutableMapOf<Long, Int>()
    private var eventId: Long = -1
    private val idList = ArrayList<Long>()
    val message = SingleLiveEvent<String>()
    val eventAndOrderIdentifier = MutableLiveData<List<Pair<Event, String>>>()
    val progress = MutableLiveData<Boolean>()

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun ordersUnderUser() {
        compositeDisposable.add(orderService.orderUser(getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.subscribe({
                    order = it
                    val query = buildQuery(it)

                    if (idList.size != 0)
                        eventsUnderUser(query)
                    else
                        progress.value = false

                }, {
                    message.value = "Failed  to list Orders under a user"
                    Timber.d(it, "Failed  to list Orders under a user ")
                }))
    }

    private fun eventsUnderUser(eventIds: String) {
        compositeDisposable.add(eventService.getEventsUnderUser(eventIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    progress.value = false
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
                    val eventAndIdentifier = ArrayList<Pair<Event, String>>()
                    order.forEach {
                        val event = eventIdMap[it.event?.id]
                        if (event != null && it.identifier != null)
                            eventAndIdentifier.add(Pair(event, it.identifier))
                    }
                    eventAndOrderIdentifier.value = eventAndIdentifier
                }, {
                    message.value = "Failed  to list events under a user"
                    Timber.d(it, "Failed  to list events under a user ")
                }))
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
