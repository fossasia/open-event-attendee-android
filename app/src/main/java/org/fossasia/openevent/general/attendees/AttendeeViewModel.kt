package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.ticket.Ticket
import timber.log.Timber

class AttendeeViewModel(private val attendeeService: AttendeeService,private val authHolder: AuthHolder) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<String>()
    val event = MutableLiveData<Event>()
    val tickets = MutableLiveData<List<Ticket>>()
    val id:Long =authHolder.getId()

    fun createAttendee(attendee: Attendee) {
        compositeDisposable.add(attendeeService.postAttendee(attendee)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    Timber.d("Success!")
                    message.value = "Attendee created successfully!"
                }, {
                    message.value = "Unable to create Attendee!"
                    Timber.d(it, "Failed")
                }))
    }

}