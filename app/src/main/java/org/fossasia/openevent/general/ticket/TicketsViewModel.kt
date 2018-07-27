package org.fossasia.openevent.general.ticket

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class TicketsViewModel(private val ticketService: TicketService,private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progressTickets = MutableLiveData<Boolean>()
    val tickets = MutableLiveData<List<Ticket>>()
    val error = MutableLiveData<String>()
    val event = MutableLiveData<Event>()

    fun loadTickets(id : Long) {
        if (id.equals(-1)) {
            error.value = "Error fetching tickets"
            return
        }
        compositeDisposable.add(ticketService.getTickets(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progressTickets.value = true
                }).subscribe({ticketList ->
                    tickets.value = ticketList
                    progressTickets.value = false
                }, {
                    error.value = "Error fetching tickets"
                    Timber.e(it, "Error fetching tickets %d",id)
                }))
    }

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable.add(eventService.getEvent(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    event.value = it
                }, {
                    Timber.e(it, "Error fetching event %d", id)
                    error.value = "Error fetching event"
                }))
    }

    fun totalTicketsEmpty(ticketIdAndQty: List<Pair<Int, Int>>): Boolean {
        return ticketIdAndQty.sumBy { it.second } == 0
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}