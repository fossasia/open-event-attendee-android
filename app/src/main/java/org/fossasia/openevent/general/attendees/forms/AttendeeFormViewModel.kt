package org.fossasia.openevent.general.attendees.forms

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.ArrayList

class AttendeeFormViewModel : ViewModel() {

    val attendeeFormData = MutableLiveData<List<AttendeeForm>>()

    fun ticketDetails(ticketDetailsAndQty: List<Triple<String, Int, Int>>?) {
        val attendeeForms = ArrayList<AttendeeForm>()
        ticketDetailsAndQty?.forEach {
            for (i in 1..it.third) {
                val attendeeForm = AttendeeForm(it.first, "", "", "")
                attendeeForms.add(attendeeForm)
            }
        }
        attendeeFormData.value = attendeeForms
    }
}