package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.ticket.TicketService
import timber.log.Timber

class AttendeeViewModel(private val attendeeService: AttendeeService, private val authHolder: AuthHolder, private val eventService: EventService, private val orderService: OrderService, private val ticketService: TicketService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<String>()
    val event = MutableLiveData<Event>()
    var attendee  = MutableLiveData<User>()

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun createAttendee(attendee: Attendee, eventId: Long, country: String, orderNotes: String, paymentOption: String) {
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
                        loadTicket(attendee.ticket!!.id, country, orderNotes, eventId, paymentOption, it.id)
                    }
                    message.value = "Attendee created successfully!"
                    Timber.d("Success!")
                }, {
                    message.value = "Unable to create Attendee!"
                    Timber.d(it, "Failed")
                }))
    }

    fun loadTicket(ticketId: Long, country: String, orderNotes: String, eventId: Long, paymentOption: String, attendeeId: Long) {
        compositeDisposable.add(ticketService.getTicketDetails(ticketId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val order = Order(
                            id = getId(),
                            paymentMode = if (it.price?.toFloat() == null || it.price.toFloat().compareTo(0) <= 0) "free" else paymentOption,
                            country = country,
                            status = "pending",
                            amount = it.price?.toFloat(),
                            orderNotes = orderNotes,
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
                    Timber.d("Success Order placing!")
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