package org.fossasia.openevent.general.attendees

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.ticket.Ticket

class AttendeeRecyclerAdapter : RecyclerView.Adapter<AttendeeViewHolder>() {
    private val attendeesAndTickets = ArrayList<Pair<Attendee, Ticket>>()
    private val customForm = ArrayList<CustomForm>()
    var formsVisibility = false

    fun addAll(attendeesAndTickets: List<Pair<Attendee, Ticket>>) {
        if (attendeesAndTickets.isNotEmpty())
            this.attendeesAndTickets.clear()
        this.attendeesAndTickets.addAll(attendeesAndTickets)
    }

    fun addCustomForm(customForm: List<CustomForm>) {
        if (customForm.isNotEmpty())
            this.customForm.clear()
        this.customForm.addAll(customForm)
    }

    fun add(attendeesAndTicket: Pair<Attendee, Ticket>) {
        this.attendeesAndTickets.add(attendeesAndTicket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendee, parent, false)
        return AttendeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        val attendeesAndTicket = attendeesAndTickets[position]
        holder.bind(attendeesAndTicket, customForm, formsVisibility)
    }

    override fun getItemCount(): Int {
        return attendeesAndTickets.size
    }
}
