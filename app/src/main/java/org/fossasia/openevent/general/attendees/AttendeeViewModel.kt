package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.ticket.Ticket
import timber.log.Timber

class AttendeeViewModel(private val attendeeService: AttendeeService, private val authHolder: AuthHolder, private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<String>()
    val event = MutableLiveData<Event>()

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun createAttendee(attendee: Attendee) {
        if (attendee.email.isNullOrEmpty() || attendee.firstname.isNullOrEmpty() || attendee.lastname.isNullOrEmpty()) {
            message.value = "Please fill all the fields"
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
                    message.value = "Attendee created successfully!"
                    Timber.d("Success!")
                }, {
                    message.value = "Unable to create Attendee!"
                    Timber.d(it, "Failed")
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

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}