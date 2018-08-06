package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.order.ConfirmOrder
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketService
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class AttendeeViewModel(private val attendeeService: AttendeeService, private val authHolder: AuthHolder, private val eventService: EventService, private val orderService: OrderService, private val ticketService: TicketService, private val authService: AuthService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()
    val ticketSoldOut = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<String>()
    val event = MutableLiveData<Event>()
    var attendee = MutableLiveData<User>()
    var paymentSelectorVisibility = MutableLiveData<Boolean>()
    var totalAmount = MutableLiveData<Float>()
    var countryVisibility = MutableLiveData<Boolean>()
    var totalQty = MutableLiveData<Int>()
    val qtyList = MutableLiveData<ArrayList<Int>>()
    val month = ArrayList<String>()
    val year = ArrayList<String>()
    val attendees = ArrayList<Attendee>()
    var country: String? = null
    lateinit var paymentOption: String
    val cardType = ArrayList<String>()
    var orderIdentifier: String? = null
    var paymentCompleted = MutableLiveData<Boolean>()
    val tickets = MutableLiveData<MutableList<Ticket>>()
    lateinit var confirmOrder: ConfirmOrder
    private val TICKET_CONFLICT_MESSAGE = "HTTP 409 CONFLICT"

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
                    countryVisibility.value = total > 0
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
                    tickets.value = it as MutableList<Ticket>?
                }, {
                    Timber.e(it, "Error Loading tickets!")
                }))
    }

    fun createAttendee(attendee: Attendee, totalAttendee: Int) {
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
                    attendees.add(it)
                    if (attendees.size == totalAttendee) {
                        loadTicketsAndCreateOrder()
                        message.value = "Attendees created successfully!"
                    }
                    Timber.d("Success! %s", attendees.toList().toString())
                }, {
                    if (it.message.equals(TICKET_CONFLICT_MESSAGE)) {
                        ticketSoldOut.value = true
                    } else {
                        message.value = "Unable to create Attendee!"
                        Timber.d(it, "Failed")
                        ticketSoldOut.value = false
                    }

                }))
    }

    fun createAttendees(attendees: List<Attendee>, country: String?, paymentOption: String) {
        this.country = country
        this.paymentOption = paymentOption
        this.attendees.clear()
        attendees.forEach {
            createAttendee(it, attendees.size)
        }
    }

    fun loadTicketsAndCreateOrder() {
        if (this.tickets.value == null) {
            this.tickets.value = ArrayList()
        }
        this.tickets.value?.clear()
        attendees?.forEach {
            loadTicket(it.ticket?.id)
        }
    }

    fun loadTicket(ticketId: Long?) {
        if (ticketId == null) {
            Timber.e("TicketId cannot be null")
            return
        }
        compositeDisposable.add(ticketService.getTicketDetails(ticketId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tickets.value?.add(it)
                    Timber.d("Loaded tickets! %s", tickets.value?.toList().toString())
                    if (tickets.value?.size == attendees?.size) {
                        createOrder()
                    }
                }, {
                    Timber.d(it, "Error loading Ticket!")
                }))
    }

    fun createOrder() {
        val attendeeList = attendees?.map { AttendeeId(it.id) }?.toList()
        var amount = totalAmount.value
        var paymentMode = paymentOption?.toLowerCase()
        if (amount == null || amount <= 0) {
            paymentMode = "free"
            amount = null
        }
        val eventId = event.value?.id
        if (eventId != null) {
            val order = Order(getId(), paymentMode, country, "pending", amount, attendees = attendeeList, event = EventId(eventId))
            compositeDisposable.add(orderService.placeOrder(order)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        progress.value = true
                    }.doFinally {
                        progress.value = false
                    }.subscribe({
                        orderIdentifier = it.identifier.toString()
                        message.value = "Order created successfully!"
                        Timber.d("Success placing order!")
                        if (it.paymentMode == "free") {
                            confirmOrder = ConfirmOrder(it.id.toString(), "completed")
                            confirmOrderStatus(it.identifier.toString(), confirmOrder)
                        }
                    }, {
                        message.value = "Unable to create Order!"
                        Timber.d(it, "Failed creating Order")
                        deleteAttendees(order.attendees)
                    }))
        } else {
            message.value = "Unable to create Order!"
        }
    }

    fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable.add(orderService.confirmOrder(identifier, order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    message.value = "Order created successfully!"
                    Timber.d("Updated order status successfully !")
                    paymentCompleted.value = true
                }, {
                    message.value = "Unable to create Order!"
                    Timber.d(it, "Failed updating order status")
                }))
    }

    fun deleteAttendees(attendeeIds: List<AttendeeId>?) {
        attendeeIds?.forEach {
            compositeDisposable.add(attendeeService.deleteAttendee(it.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Timber.d("Deleted attendee $it.id")
                    }, {
                        Timber.d("Failed to delete attendee $it.id")
                    }))
        }
    }

    fun completeOrder(charge: Charge) {
        compositeDisposable.add(orderService.chargeOrder(orderIdentifier.toString(), charge)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    message.value = it.message

                    if (it.status != null && it.status) {
                        confirmOrderStatus(orderIdentifier.toString(), confirmOrder)
                        Timber.d("Successfully  charged for the order!")
                    } else {
                        Timber.d("Failed charging the user")
                    }
                }, {
                    message.value = "Payment not completed!"
                    Timber.d(it, "Failed charging the user")
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

    fun logout() {
        compositeDisposable.add(authService.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Logged out!")
                }) {
                    Timber.e(it, "Failure Logging out!")
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}