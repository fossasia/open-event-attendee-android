package org.fossasia.openevent.general.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class TicketsViewModel(
    private val ticketService: TicketService,
    private val eventService: EventService,
    private val authService: AuthService,
    private val authHolder: AuthHolder,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableIsUserVerified = MutableLiveData<Boolean>()
    val isUserVerified: LiveData<Boolean> = mutableIsUserVerified
    private val mutableAmount = MutableLiveData<Float>()
    val amount: LiveData<Float> = mutableAmount
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
                mutableProgress.value = true
            }.subscribe({ ticketList ->
                mutableProgress.value = false
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

    fun getProfile() {
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                if (user.isVerified) {
                    mutableIsUserVerified.value = true
                } else {
                    syncProfile()
                }
            }) {
                Timber.e(it, "Failure")
                mutableError.value = resource.getString(R.string.failure)
            }
    }

    private fun syncProfile() {
        compositeDisposable += authService.syncProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                Timber.d("Response Success")
                mutableIsUserVerified.value = user.isVerified
            }) {
                Timber.e(it, "Failure")
            }
    }

    fun getAmount(ticketIdAndQty: List<Pair<Int, Int>>) {
        val ticketIds = ArrayList<Int>()
        val qty = ArrayList<Int>()
        ticketIdAndQty.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
                qty.add(it.second)
            }
        }
        compositeDisposable += ticketService.getTicketsWithIds(ticketIds)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ tickets ->
                var prices = 0F
                var index = 0
                tickets.forEach {
                    it.price?.let { price -> prices += price * qty[index++] }
                }
                mutableAmount.value = prices
            }, {
                Timber.e(it, "Error Loading tickets!")
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
