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
import org.fossasia.openevent.general.discount.DiscountCode
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import retrofit2.HttpException
import timber.log.Timber

class TicketsViewModel(
    private val ticketService: TicketService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    val tickets = MutableLiveData<List<Ticket>>()
    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableDiscountCodes = MutableLiveData<DiscountCode>()
    val discountCode: LiveData<DiscountCode> = mutableDiscountCodes
    var appliedDiscountCode: DiscountCode? = null
    val mutableAmount = MutableLiveData<Float>()
    val amount: LiveData<Float> = mutableAmount
    private val mutableTicketTableVisibility = MutableLiveData<Boolean>()
    val ticketTableVisibility: LiveData<Boolean> = mutableTicketTableVisibility
    val ticketIdAndQty = MutableLiveData<List<Triple<Int, Int, Float>>>()
    var discountCodeCurrentLayout = APPLY_DISCOUNT_CODE
    var hasPaidTickets = false

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
                mutableProgress.value = false
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

    fun fetchDiscountCode(code: String) {
        compositeDisposable += ticketService.getDiscountCode(code)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableDiscountCodes.value = it
            }, {
                Timber.e(it, "Error fetching discount codes %s", code)
                if ((it as HttpException).code() == 404)
                    mutableError.value = resource.getString(R.string.invalid_discount_code)
                else
                    mutableError.value = resource.getString(R.string.error_fetching_discount_code)
            })
    }

    fun getAmount(ticketIdAndQty: List<Triple<Int, Int, Float>>) {
        val ticketIds = ArrayList<Int>()
        val qty = ArrayList<Int>()
        ticketIdAndQty.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
                qty.add(it.second)
            }
        }
        val donation = ticketIdAndQty.map { it.third*it.second }.sum()
        compositeDisposable += ticketService.getTicketsWithIds(ticketIds)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ tickets ->
                var prices = 0F
                var index = 0
                val code = appliedDiscountCode
                tickets.forEach { ticket ->
                    var price = ticket.price
                    if (code?.value != null) {
                        appliedDiscountCode?.tickets?.forEach { ticketId ->
                            if (ticket.id == ticketId.id.toInt()) {
                                price -= if (code.type == AMOUNT) code.value else price*(code.value / 100)
                            }
                        }
                    }
                    price.let { prices += price * qty[index++] }
                    if (ticket.type == TICKET_TYPE_PAID)
                        hasPaidTickets = true
                }
                mutableAmount.value = prices + donation
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun totalTicketsEmpty(ticketIdAndQty: List<Triple<Int, Int, Float>>): Boolean {
        return ticketIdAndQty.sumBy { it.second } == 0
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
