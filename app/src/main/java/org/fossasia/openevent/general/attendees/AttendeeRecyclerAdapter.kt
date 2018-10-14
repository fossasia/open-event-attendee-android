package org.fossasia.openevent.general.attendees

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.Ticket

class AttendeeRecyclerAdapter : RecyclerView.Adapter<AttendeeViewHolder>() {
    val attendeeList = ArrayList<Attendee>()
    val ticketList = ArrayList<Ticket>()
    var eventId = EventId(-1)
    val customForm = ArrayList<CustomForm>()

    fun addAll(attendeeList: List<Attendee>, ticketList: List<Ticket>) {
        if (attendeeList.isNotEmpty())
            this.attendeeList.clear()
        this.attendeeList.addAll(attendeeList)
        if (ticketList.isNotEmpty())
            this.ticketList.clear()
        this.ticketList.addAll(ticketList)
    }

    fun setCustomForm(customForm: List<CustomForm>) {
        this.customForm.clear()
        this.customForm.addAll(customForm)
    }

    fun add(attendeeList: Attendee, ticket: Ticket) {
        this.attendeeList.add(attendeeList)
        this.ticketList.add(ticket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendee, parent, false)
        return AttendeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        holder.bind(this, position)
    }

    override fun getItemCount(): Int {
        return attendeeList.size
    }
}
