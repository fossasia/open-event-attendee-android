package org.fossasia.openevent.general.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class TicketsViewModel(
    private val ticketService: TicketService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgressTickets = MutableLiveData<Boolean>()
    val progressTickets: LiveData<Boolean> = mutableProgressTickets
    val tickets = MutableLiveData<List<Ticket>>()
    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableTicketTableVisibility = MutableLiveData<Boolean>()
    val ticketTableVisibility: LiveData<Boolean> = mutableTicketTableVisibility
    val ticketIdAndQty = MutableLiveData<List<Pair<Int, Int>>>()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun loadTickets(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_tickets_message)
            return
        }
        compositeDisposable += ticketService.getTickets(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgressTickets.value = true
            }.subscribe({ ticketList ->
                mutableProgressTickets.value = false
                mutableTicketTableVisibility.value = ticketList.isNotEmpty()
                tickets.value = ticketList
            }, {
                mutableError.value = resource.getString(R.string.error_fetching_tickets_message)
                Timber.e(it, "Error fetching tickets %d", id)
            })
    }

    fun loadEvent(id: Long) {
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable += eventService.getEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun totalTicketsEmpty(ticketIdAndQty: List<Pair<Int, Int>>): Boolean {
        return ticketIdAndQty.sumBy { it.second } == 0
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
