package org.fossasia.openevent.general.attendees

import android.util.Patterns
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
import org.fossasia.openevent.general.settings.SettingsService
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketService
import org.fossasia.openevent.general.utils.HttpErrors
import retrofit2.HttpException
import timber.log.Timber

const val ORDER_STATUS_PENDING = "pending"
const val ORDER_STATUS_COMPLETED = "completed"
const val ORDER_STATUS_PLACED = "placed"
const val ORDER_STATUS_CANCELLED = "cancelled"
const val ORDER_STATUS_INITIALIZING = "initializing"
const val PAYMENT_MODE_FREE = "free"
const val PAYMENT_MODE_BANK = "bank"
const val PAYMENT_MODE_ONSITE = "onsite"
const val PAYMENT_MODE_CHEQUE = "cheque"
const val PAYMENT_MODE_PAYPAL = "paypal"
const val PAYMENT_MODE_STRIPE = "stripe"
private const val ORDER_EXPIRY_TIME = 15

class AttendeeViewModel(
    private val attendeeService: AttendeeService,
    private val authHolder: AuthHolder,
    private val eventService: EventService,
    private val orderService: OrderService,
    private val ticketService: TicketService,
    private val authService: AuthService,
    private val settingsService: SettingsService,
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
    val orderCompleted = MutableLiveData<Boolean>()
    val totalAmount = MutableLiveData(0F)
    private val mutableTickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = mutableTickets
    private val mutableForms = MutableLiveData<List<CustomForm>>()
    val forms: LiveData<List<CustomForm>> = mutableForms
    private val mutablePendingOrder = MutableLiveData<Order>()
    val pendingOrder: LiveData<Order> = mutablePendingOrder
    private val mutableStripeOrderMade = MutableLiveData<Boolean>()
    val stripeOrderMade: LiveData<Boolean> = mutableStripeOrderMade
    private val mutableOrderExpiryTime = MutableLiveData<Int>()
    val orderExpiryTime: LiveData<Int> = mutableOrderExpiryTime

    val attendees = ArrayList<Attendee>()
    private val attendeesForOrder = ArrayList<Attendee>()
    private val ticketsForOrder = ArrayList<Ticket>()
    private var paymentModeForOrder: String = PAYMENT_MODE_FREE
    private var countryForOrder: String = ""
    private var companyForOrder: String = ""
    private var taxIdForOrder: String = ""
    private var addressForOrder: String = ""
    private var cityForOrder: String = ""
    private var postalCodeForOrder: String = ""

    private var createAttendeeIterations = 0
    var orderIdentifier: String? = null
        private set
    private lateinit var confirmOrder: ConfirmOrder

    // Retained information
    var countryPosition: Int = -1
    var ticketIdAndQty: List<Triple<Int, Int, Float>>? = null
    var selectedPaymentOption: Int = -1
    var singleTicket = false
    var monthSelectedPosition: Int = 0
    var yearSelectedPosition: Int = 0
    var paymentCurrency: String = ""
    var timeout: Long = -1L
    var ticketDetailsVisible = false
    var billingEnabled = false

    // Log in Information
    private val mutableSignedIn = MutableLiveData(true)
    val signedIn: LiveData<Boolean> = mutableSignedIn
    var isShowingSignInText = true

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun login(email: String, password: String) {

        compositeDisposable += authService.login(email, password)
            .withDefaultSchedulers()
            .subscribe({
                mutableSignedIn.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.login_fail_message)
            })
    }

    fun logOut() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                mutableSignedIn.value = false
                mutableUser.value = null
            }) {
                Timber.e(it, "Failure Logging out!")
            }
    }

    fun getTickets() {
        val ticketIds = ArrayList<Int>()
        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
            }
        }

        compositeDisposable += ticketService.getTicketsWithIds(ticketIds)
            .withDefaultSchedulers()
            .subscribe({ tickets ->
                mutableTickets.value = tickets
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
    }

    fun initializeOrder(eventId: Long) {
        val emptyOrder = Order(id = getId(), status = ORDER_STATUS_INITIALIZING, event = EventId(eventId))

        compositeDisposable += orderService.placeOrder(emptyOrder)
            .withDefaultSchedulers()
            .subscribe({
                mutablePendingOrder.value = it
                orderIdentifier = it.identifier.toString()
            }, {
                Timber.e(it, "Fail on creating pending order")
            })
    }

    private fun createAttendee(attendee: Attendee, totalAttendee: Int) {
        compositeDisposable += attendeeService.postAttendee(attendee)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                attendeesForOrder.add(it)
                if (attendeesForOrder.size == totalAttendee) {
                    loadTicketsAndCreateOrder()
                    mutableMessage.value = resource.getString(R.string.create_attendee_success_message)
                }
                Timber.d("Success! %s", attendeesForOrder.toList().toString())
            }, {
                mutableProgress.value = false
                if (createAttendeeIterations + 1 == totalAttendee)
                    if (it is HttpException) {
                        if (it.code() == HttpErrors.CONFLICT)
                            mutableTicketSoldOut.value = true
                    } else {
                        mutableMessage.value = resource.getString(R.string.create_attendee_fail_message)
                        Timber.d(it, "Failed")
                        mutableTicketSoldOut.value = false
                    }
            })
    }

    fun createAttendees(
        attendees: List<Attendee>,
        country: String?,
        company: String,
        taxId: String,
        address: String,
        city: String,
        postalCode: String,
        paymentMode: String
    ) {
        attendeesForOrder.clear()
        countryForOrder = country ?: ""
        companyForOrder = company
        taxIdForOrder = taxId
        addressForOrder = address
        cityForOrder = city
        postalCodeForOrder = postalCode
        paymentModeForOrder = paymentMode
        var isAllDetailsFilled = true
        createAttendeeIterations = 0
        attendees.forEach {
            if (it.email.isNullOrBlank() || it.firstname.isNullOrBlank() || it.lastname.isNullOrBlank()) {
                if (isAllDetailsFilled)
                    mutableMessage.value = resource.getString(R.string.fill_all_fields_message)
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
        var order = mutablePendingOrder.value
        val identifier: String? = orderIdentifier
        if (order == null || identifier == null) {
            mutableMessage.value = resource.getString(R.string.order_fail_message)
            return
        }
        val attendeeList = attendeesForOrder.map { AttendeeId(it.id) }.toList()
        val amount: Float = totalAmount.value ?: 0F
        if (amount <= 0) {
            paymentModeForOrder = PAYMENT_MODE_FREE
        }
        order = order.copy(attendees = attendeeList, paymentMode = paymentModeForOrder, amount = amount)
        if (billingEnabled) {
            order = order.copy(isBillingEnabled = true, company = companyForOrder, taxBusinessInfo = taxIdForOrder,
                address = addressForOrder, city = cityForOrder, zipcode = postalCodeForOrder, country = countryForOrder)
        }
        compositeDisposable += orderService.placeOrder(order)
            .withDefaultSchedulers()
            .subscribe({
                orderIdentifier = it.identifier.toString()
                Timber.d("Success placing order!")
                when (it.paymentMode) {
                    PAYMENT_MODE_FREE -> {
                        confirmOrder = ConfirmOrder(it.id.toString(), ORDER_STATUS_COMPLETED)
                        confirmOrderStatus(it.identifier.toString(), confirmOrder)
                    }
                    PAYMENT_MODE_CHEQUE, PAYMENT_MODE_BANK, PAYMENT_MODE_ONSITE -> {
                        confirmOrder = ConfirmOrder(it.id.toString(), ORDER_STATUS_PLACED)
                        confirmOrderStatus(it.identifier.toString(), confirmOrder)
                    }
                    PAYMENT_MODE_STRIPE -> {
                        mutableStripeOrderMade.value = true
                    }
                    else -> mutableMessage.value = resource.getString(R.string.order_success_message)
                }
            }, {
                mutableMessage.value = resource.getString(R.string.order_fail_message)
                Timber.d(it, "Failed creating Order")
                mutableProgress.value = false
                deleteAttendees(order.attendees)
            })
    }

    private fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable += orderService.confirmOrder(identifier, order)
            .withDefaultSchedulers()
            .doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = resource.getString(R.string.order_success_message)
                Timber.d("Updated order status successfully !")
                orderCompleted.value = true
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

    fun chargeOrder(charge: Charge) {
        compositeDisposable += orderService.chargeOrder(orderIdentifier.toString(), charge)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
                if (it.status != null && it.status) {
                    confirmOrder = ConfirmOrder(it.id.toString(), ORDER_STATUS_COMPLETED)
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

    fun areAttendeeEmailsValid(attendees: ArrayList<Attendee>): Boolean {
        /**Checks for  correct pattern in email*/
        attendees.forEach {
            if (it.email.isNullOrEmpty()) return false
            else if (!Patterns.EMAIL_ADDRESS.matcher(it.email).matches()) return false
        }
        return true
    }

    fun getSettings() {
        compositeDisposable += settingsService.fetchSettings()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableOrderExpiryTime.value = it.orderExpiryTime
            }, {
                mutableOrderExpiryTime.value = ORDER_EXPIRY_TIME
                Timber.e(it, "Error fetching settings")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
