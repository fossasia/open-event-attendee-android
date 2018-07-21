package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketService
import timber.log.Timber
import java.util.*

class AttendeeViewModel(private val attendeeService: AttendeeService, private val authHolder: AuthHolder, private val eventService: EventService, private val orderService: OrderService, private val ticketService: TicketService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<String>()
    val event = MutableLiveData<Event>()
    var attendee = MutableLiveData<User>()
    val tickets = MutableLiveData<List<Ticket>>()
    var paymentSelectorVisibility = MutableLiveData<Boolean>()
    var totalAmount = MutableLiveData<Float>()
    var totalQty = MutableLiveData<Int>()
    val qtyList = MutableLiveData<ArrayList<Int>>()
    val month = ArrayList<String>()
    val year = ArrayList<String>()
    val cardType = ArrayList<String>()

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun initializeSpinner() {
        // initialize months
        month.add("Month")
        month.add("January")
        month.add("February")
        month.add("March")
        month.add("April")
        month.add("May")
        month.add("June")
        month.add("July")
        month.add("August")
        month.add("September")
        month.add("October")
        month.add("November")
        month.add("December")

        // initialize years
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        year.add("Year")
        val a = currentYear + 20
        for (i in currentYear..a) {
            year.add(i.toString())
        }

        // initialize card types
        cardType.add("Select a card type")
        cardType.add("Pay by American Express")
        cardType.add("Pay by MasterCard")
        cardType.add("Pay by Visa")
    }

    fun updatePaymentSelectorVisibility(ticketIdAndQty: List<Pair<Int, Int>>?) {
        val ticketIds = ArrayList<Int>()
        val qty = ArrayList<Int>()
        totalQty.value = 0

        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
                qty.add(it.second)
                totalQty.value = totalQty.value?.plus(it.second)
            }
        }
        qtyList.value = qty

        compositeDisposable.add(ticketService.getTicketPriceWithIds(ticketIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    var total = 0.toFloat()
                    var index = 0
                    it?.forEach {
                        total += it * qty[index++]
                    }
                    totalAmount.value = total
                    paymentSelectorVisibility.value = total != 0.toFloat()
                }, {
                    Timber.e(it, "Error Loading tickets!")
                }))
    }

    fun ticketDetails(ticketIdAndQty: List<Pair<Int, Int>>?) {
        val ticketIds = ArrayList<Int>()
        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
            }
        }

        compositeDisposable.add(ticketService.getTicketsWithIds(ticketIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tickets.value = it
                }, {
                    Timber.e(it, "Error Loading tickets!")
                }))
    }

    fun createAttendee(attendee: Attendee, eventId: Long, country: String, paymentOption: String) {
        if (attendee.email.isNullOrEmpty() || attendee.firstname.isNullOrEmpty() || attendee.lastname.isNullOrEmpty()) {
            message.value = "Please fill in all the fields"
            return
        }

        compositeDisposable.add(attendeeService.postAttendee(attendee)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    if (attendee.ticket?.id != null) {
                        loadTicket(attendee.ticket?.id, country, eventId, paymentOption, it.id)
                    }
                    message.value = "Attendee created successfully!"
                    Timber.d("Success! %s", it.id)
                }, {
                    message.value = "Unable to create Attendee!"
                    Timber.d(it, "Failed")
                }))
    }

    fun loadTicket(ticketId: Long?, country: String, eventId: Long, paymentOption: String, attendeeId: Long) {
        if (ticketId == null) {
            Timber.e("TicketId cannot be null")
            return
        }
        compositeDisposable.add(ticketService.getTicketDetails(ticketId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val order = Order(
                            id = getId(),
                            paymentMode = if (it.price == null || it.price <= 0) "free" else paymentOption.toLowerCase(),
                            country = country,
                            status = "pending",
                            amount = if (it.price == null || it.price <= 0) null else it.price,
                            attendees = arrayListOf(AttendeeId(attendeeId)),
                            event = EventId(eventId))
                    createOrder(order)
                }, {
                    Timber.d(it, "Error loading Ticket!")
                }))
    }

    fun createOrder(order: Order) {
        compositeDisposable.add(orderService.placeOrder(order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    message.value = "Order created successfully!"
                    Timber.d("Success placing order!")
                }, {
                    message.value = "Unable to create Order!"
                    Timber.d(it, "Failed creating Order")
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
                    message.value = "Error fetching event"
                }))
    }

    fun loadUser(id: Long) {
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable.add(attendeeService.getAttendeeDetails(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    attendee.value = it
                }, {
                    Timber.e(it, "Error fetching user %d", id)
                }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}