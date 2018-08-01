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
    val message = SingleLiveEvent<String>()
    val order = MutableLiveData<List<Order>>()
    val event = MutableLiveData<List<Event>>()
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
                    order.value = it
                    val idList = ArrayList<Long>()
                    var subQuery = ""
                    var eventId: Long = -1

                    it.forEach {
                        it.event?.id?.let { it1 ->
                            idList.add(it1)
                            eventId = it1
                            subQuery += ",{\"name\":\"id\",\"op\":\"eq\",\"val\":\"$eventId\"}"
                        }
                    }
                    val query = buildQuery(idList, eventId, subQuery)

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
                    event.value = it
                }, {
                    message.value = "Failed  to list events under a user"
                    Timber.d(it, "Failed  to list events under a user ")
                }))
    }

    private fun buildQuery(idList: ArrayList<Long>, eventId: Long, subQuery: String): String {
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
