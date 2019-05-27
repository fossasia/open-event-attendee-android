package org.fossasia.openevent.general.attendees

import android.util.Patterns
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.R
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.order.ConfirmOrder
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketService
import org.fossasia.openevent.general.utils.HttpErrors
import timber.log.Timber

class AttendeeViewModel(
    private val attendeeService: AttendeeService,
    private val authHolder: AuthHolder,
    private val eventService: EventService,
    private val orderService: OrderService,
    private val ticketService: TicketService,
    private val authService: AuthService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableTicketSoldOut = MutableLiveData<Boolean>()
    val ticketSoldOut: LiveData<Boolean> = mutableTicketSoldOut
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableTotalAmount = MutableLiveData<Float>(0F)
    val totalAmount: LiveData<Float> = mutableTotalAmount
    val paymentCompleted = MutableLiveData<Boolean>()
    private val mutableTickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = mutableTickets
    private val mutableForms = MutableLiveData<List<CustomForm>>()
    val forms: LiveData<List<CustomForm>> = mutableForms
    private val mutableIsAttendeeCreated = MutableLiveData<Boolean>()
    val isAttendeeCreated: LiveData<Boolean> = mutableIsAttendeeCreated

    val attendees = ArrayList<Attendee>()
    private val attendeesForOrder = ArrayList<Attendee>()
    private val ticketsForOrder = ArrayList<Ticket>()
    private lateinit var paymentOption: String
    private lateinit var countryForOrder: String

    private var createAttendeeIterations = 0
    var orderIdentifier: String? = null
        private set
    private lateinit var confirmOrder: ConfirmOrder

    // Retained information
    var countryPosition: Int = -1
    var ticketIdAndQty: List<Pair<Int, Int>>? = null
    var selectedPaymentOption: Int = -1
    var singleTicket = false
    var monthSelectedPosition: Int = 0
    var yearSelectedPosition: Int = 0
    var cardTypePosition: Int = 0
    var identifierList = ArrayList<String>()
    var editTextList = ArrayList<EditText>()
    var paymentCurrency: String = ""
    var ticketDetailsVisible = false

    fun getId() = authHolder.getId()

    fun getTickets() {
        val ticketIds = ArrayList<Int>()
        val qty = ArrayList<Int>()
        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
                qty.add(it.second)
            }
        }

        compositeDisposable += ticketService.getTicketsWithIds(ticketIds)
            .withDefaultSchedulers()
            .subscribe({ tickets ->
                var prices = 0F
                var index = 0
                tickets.forEach {
                    it.price?.let { price -> prices += price * qty[index++] }
                }
                mutableTickets.value = tickets
                mutableTotalAmount.value = prices
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
    }

    private fun createAttendee(attendee: Attendee, totalAttendee: Int) {
        compositeDisposable += attendeeService.postAttendee(attendee)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                createAttendeeIterations++
                if (createAttendeeIterations == totalAttendee)
                    mutableProgress.value = false
            }.subscribe({
                attendeesForOrder.add(it)
                if (attendeesForOrder.size == totalAttendee) {
                    loadTicketsAndCreateOrder()
                    mutableIsAttendeeCreated.value = true
                    mutableMessage.value = resource.getString(R.string.create_attendee_success_message)
                }
                Timber.d("Success! %s", attendeesForOrder.toList().toString())
            }, {
                if (createAttendeeIterations + 1 == totalAttendee)
                    if (it.message.equals(HttpErrors.CONFLICT)) {
                        mutableTicketSoldOut.value = true
                    } else {
                        mutableMessage.value = resource.getString(R.string.create_attendee_fail_message)
                        Timber.d(it, "Failed")
                        mutableTicketSoldOut.value = false
                    }
            })
    }

    fun createAttendees(attendees: List<Attendee>, country: String?, paymentOption: String) {
        attendeesForOrder.clear()
        this.countryForOrder = country ?: ""
        this.paymentOption = paymentOption
        var isAllDetailsFilled = true
        createAttendeeIterations = 0
        attendees.forEach {
            if (it.email.isNullOrBlank() || it.firstname.isNullOrBlank() || it.lastname.isNullOrBlank()) {
                if (isAllDetailsFilled)
                    mutableMessage.value = resource.getString(R.string.fill_all_fields_message)
                mutableIsAttendeeCreated.value = false
                isAllDetailsFilled = false
                return
            }
        }
        if (isAllDetailsFilled) {
            attendees.forEach {
                createAttendee(it, attendees.size)
            }
        }
    }

    private fun loadTicketsAndCreateOrder() {
        ticketsForOrder.clear()
        attendeesForOrder.forEach {
            loadTicket(it.ticket?.id)
        }
    }

    private fun loadTicket(ticketId: Long?) {
        if (ticketId == null) {
            Timber.e("TicketId cannot be null")
            return
        }
        compositeDisposable += ticketService.getTicketDetails(ticketId)
            .withDefaultSchedulers()
            .subscribe({
                ticketsForOrder.add(it)
                Timber.d("Loaded tickets! %s", ticketsForOrder.toList().toString())
                if (ticketsForOrder.size == attendeesForOrder.size) {
                    createOrder()
                }
            }, {
                Timber.d(it, "Error loading Ticket!")
            })
    }

    private fun createOrder() {
        val attendeeList = attendeesForOrder.map { AttendeeId(it.id) }.toList()
        val amount: Float = totalAmount.value ?: 0F
        var paymentMode: String? = paymentOption.toLowerCase()
        if (amount <= 0) {
            paymentMode = resource.getString(R.string.free)
        }
        val eventId = event.value?.id
        if (eventId != null) {
            val order = Order(
                getId(), paymentMode, countryForOrder, "pending", amount,
                attendees = attendeeList, event = EventId(eventId)
            )
            compositeDisposable += orderService.placeOrder(order)
                .withDefaultSchedulers()
                .doOnSubscribe {
                    mutableProgress.value = true
                }.doFinally {
                    mutableProgress.value = false
                }.subscribe({
                    orderIdentifier = it.identifier.toString()
                    Timber.d("Success placing order!")
                    if (it.paymentMode == resource.getString(R.string.free)) {
                        confirmOrder = ConfirmOrder(it.id.toString(), "completed")
                        confirmOrderStatus(it.identifier.toString(), confirmOrder)
                    } else mutableMessage.value = resource.getString(R.string.order_success_message)
                }, {
                    mutableMessage.value = resource.getString(R.string.order_fail_message)
                    Timber.d(it, "Failed creating Order")
                    deleteAttendees(order.attendees)
                })
        } else {
            mutableMessage.value = resource.getString(R.string.order_fail_message)
        }
    }

    private fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable += orderService.confirmOrder(identifier, order)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = resource.getString(R.string.order_success_message)
                Timber.d("Updated order status successfully !")
                paymentCompleted.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.order_fail_message)
                Timber.d(it, "Failed updating order status")
            })
    }

    fun getCustomFormsForAttendees(eventId: Long) {
        compositeDisposable += attendeeService.getCustomFormsForAttendees(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableForms.value = it
                Timber.d("Forms fetched successfully !")
            }, {
                Timber.d(it, "Failed fetching forms")
            })
    }

    private fun deleteAttendees(attendeeIds: List<AttendeeId>?) {
        attendeeIds?.forEach { attendeeId ->
            compositeDisposable += attendeeService.deleteAttendee(attendeeId.id)
                .withDefaultSchedulers()
                .subscribe({
                    Timber.d("Deleted attendee $attendeeId.id")
                }, {
                    Timber.d("Failed to delete attendee $it.id")
                })
        }
    }

    fun completeOrder(charge: Charge) {
        compositeDisposable += orderService.chargeOrder(orderIdentifier.toString(), charge)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
                if (it.status != null && it.status) {
                    confirmOrderStatus(orderIdentifier.toString(), confirmOrder)
                    Timber.d("Successfully  charged for the order!")
                } else {
                    Timber.d("Failed charging the user")
                }
            }, {
                mutableMessage.value = resource.getString(R.string.payment_not_complete_message)
                Timber.d(it, "Failed charging the user")
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
                mutableMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadUser() {
        val id = getId()
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable += attendeeService.getAttendeeDetails(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableUser.value = it
            }, {
                Timber.e(it, "Error fetching user %d", id)
            })
    }

    fun logout() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Logged out!")
            }) {
                Timber.e(it, "Failure Logging out!")
            }
    }

    fun areAttendeeEmailsValid(attendees: ArrayList<Attendee>): Boolean {
        /**Checks for  correct pattern in email*/
        attendees.forEach {
            if (it.email.isNullOrEmpty()) return false
            else if (!Patterns.EMAIL_ADDRESS.matcher(it.email).matches()) return false
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
