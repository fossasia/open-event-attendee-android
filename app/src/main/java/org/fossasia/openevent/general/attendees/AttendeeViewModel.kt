package org.fossasia.openevent.general.attendees

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
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
import java.util.Calendar

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
    private val mutableAttendee = MutableLiveData<User>()
    val attendee: LiveData<User> = mutableAttendee
    private val mutablePaymentSelectorVisibility = MutableLiveData<Boolean>()
    val paymentSelectorVisibility: LiveData<Boolean> = mutablePaymentSelectorVisibility
    private val mutableTotalAmount = MutableLiveData<Float>()
    val totalAmount: LiveData<Float> = mutableTotalAmount
    private val mutableCountryVisibility = MutableLiveData<Boolean>()
    val countryVisibility: LiveData<Boolean> = mutableCountryVisibility
    private val mutableTotalQty = MutableLiveData<Int>()
    val totalQty: LiveData<Int> = mutableTotalQty
    private val mutableQtyList = MutableLiveData<ArrayList<Int>>()
    val qtyList: LiveData<ArrayList<Int>> = mutableQtyList
    val paymentCompleted = MutableLiveData<Boolean>()
    val ticketDetailsVisibility = MutableLiveData<Boolean>()
    private val mutableTickets = MutableLiveData<MutableList<Ticket>>()
    val tickets: LiveData<MutableList<Ticket>> = mutableTickets
    private val mutableForms = MutableLiveData<List<CustomForm>>()
    val forms: LiveData<List<CustomForm>> = mutableForms
    private val mutableIsAttendeeCreated = MutableLiveData<Boolean>()
    val isAttendeeCreated: LiveData<Boolean> = mutableIsAttendeeCreated

    val month = ArrayList<String?>()
    val year = ArrayList<String?>()
    val attendees = ArrayList<Attendee>()
    val cardType = ArrayList<String?>()
    var isAllDetailsFilled = true

    private var createAttendeeIterations = 0
    var country: String? = null
        private set
    var orderIdentifier: String? = null
        private set
    private lateinit var paymentOption: String
    private lateinit var confirmOrder: ConfirmOrder

    fun getId() = authHolder.getId()

    fun initializeSpinner() {
        // initialize months
        month.add(resource.getString(R.string.month_string))
        month.add(resource.getString(R.string.january))
        month.add(resource.getString(R.string.february))
        month.add(resource.getString(R.string.march))
        month.add(resource.getString(R.string.april))
        month.add(resource.getString(R.string.may))
        month.add(resource.getString(R.string.june))
        month.add(resource.getString(R.string.july))
        month.add(resource.getString(R.string.august))
        month.add(resource.getString(R.string.september))
        month.add(resource.getString(R.string.october))
        month.add(resource.getString(R.string.november))
        month.add(resource.getString(R.string.december))

        // initialize years
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        year.add(resource.getString(R.string.year_string))
        val a = currentYear + 20
        for (i in currentYear..a) {
            year.add(i.toString())
        }

        // initialize card types
        cardType.add(resource.getString(R.string.select_card))
        cardType.add(resource.getString(R.string.american_express_pay_message))
        cardType.add(resource.getString(R.string.mastercard_pay_message))
        cardType.add(resource.getString(R.string.visa_pay_message))
        cardType.add(resource.getString(R.string.discover_pay_message))
        cardType.add(resource.getString(R.string.diners_pay_message))
        cardType.add(resource.getString(R.string.unionpay_pay_message))
    }

    fun updatePaymentSelectorVisibility(ticketIdAndQty: List<Pair<Int, Int>>?) {
        val ticketIds = ArrayList<Int>()
        val qty = ArrayList<Int>()
        mutableTotalQty.value = 0

        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
                qty.add(it.second)
                mutableTotalQty.value = totalQty.value?.plus(it.second)
            }
        }
        mutableQtyList.value = qty

        compositeDisposable.add(ticketService.getTicketPriceWithIds(ticketIds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ prices ->
                var total = 0.toFloat()
                var index = 0
                prices?.forEach {
                    total += it * qty[index++]
                }
                mutableTotalAmount.value = total
                mutableCountryVisibility.value = total > 0
                mutablePaymentSelectorVisibility.value = total != 0.toFloat()
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
        )
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
                mutableTickets.value = it as MutableList<Ticket>?
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
        )
    }

    private fun createAttendee(attendee: Attendee, totalAttendee: Int) {
        compositeDisposable.add(attendeeService.postAttendee(attendee)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                createAttendeeIterations++
                if (createAttendeeIterations == totalAttendee)
                    mutableProgress.value = false
            }.subscribe({
                attendees.add(it)
                if (attendees.size == totalAttendee) {
                    loadTicketsAndCreateOrder()
                    mutableIsAttendeeCreated.value = true
                    mutableMessage.value = resource.getString(R.string.create_attendee_success_message)
                }
                Timber.d("Success! %s", attendees.toList().toString())
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
        )
    }

    fun createAttendees(attendees: List<Attendee>, country: String?, paymentOption: String) {
        this.country = country
        this.paymentOption = paymentOption
        this.attendees.clear()
        isAllDetailsFilled = true
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
        if (this.tickets.value == null) {
            this.mutableTickets.value = ArrayList()
        }
        this.tickets.value?.clear()
        attendees.forEach {
            loadTicket(it.ticket?.id)
        }
    }

    private fun loadTicket(ticketId: Long?) {
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
                if (tickets.value?.size == attendees.size) {
                    createOrder()
                }
            }, {
                Timber.d(it, "Error loading Ticket!")
            })
        )
    }

    private fun createOrder() {
        val attendeeList = attendees.map { AttendeeId(it.id) }.toList()
        var amount = totalAmount.value
        var paymentMode: String? = paymentOption.toLowerCase()
        if (amount == null || amount <= 0) {
            paymentMode = resource.getString(R.string.free)
            amount = null
        }
        val eventId = event.value?.id
        if (eventId != null) {
            val order = Order(
                getId(), paymentMode, country, "pending", amount,
                attendees = attendeeList, event = EventId(eventId)
            )
            compositeDisposable.add(orderService.placeOrder(order)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
            )
        } else {
            mutableMessage.value = resource.getString(R.string.order_fail_message)
        }
    }

    private fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable.add(orderService.confirmOrder(identifier, order)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
        )
    }

    fun getCustomFormsForAttendees(eventId: Long) {
        val filter = "[{\"name\":\"form\",\"op\":\"eq\",\"val\":\"order\"}]"
        compositeDisposable.add(attendeeService.getCustomFormsForAttendees(eventId, filter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableForms.value = it
                Timber.d("Forms fetched successfully !")
            }, {
                Timber.d(it, "Failed fetching forms")
            })
        )
    }

    private fun deleteAttendees(attendeeIds: List<AttendeeId>?) {
        attendeeIds?.forEach { attendeeId ->
            compositeDisposable.add(attendeeService.deleteAttendee(attendeeId.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Deleted attendee $attendeeId.id")
                }, {
                    Timber.d("Failed to delete attendee $it.id")
                })
            )
        }
    }

    fun completeOrder(charge: Charge) {
        compositeDisposable.add(orderService.chargeOrder(orderIdentifier.toString(), charge)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
        )
    }

    fun loadEvent(id: Long) {
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable.add(eventService.getEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event %d", id)
                mutableMessage.value = "Error fetching event"
            })
        )
    }

    fun loadUser() {
        val id = getId()
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable.add(attendeeService.getAttendeeDetails(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableAttendee.value = it
            }, {
                Timber.e(it, "Error fetching user %d", id)
            })
        )
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
