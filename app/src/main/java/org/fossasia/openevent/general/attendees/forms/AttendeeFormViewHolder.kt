package org.fossasia.openevent.general.attendees.forms

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_attendee_form.view.*

class AttendeeFormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(attendeeForm: AttendeeForm) {
        itemView.ticketName.text = "Ticket ${adapterPosition+1} - ${attendeeForm.ticketName}"
    }
}