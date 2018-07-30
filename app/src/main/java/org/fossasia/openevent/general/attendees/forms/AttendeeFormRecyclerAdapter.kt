package org.fossasia.openevent.general.attendees.forms

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.ArrayList

class AttendeeFormRecyclerAdapter : RecyclerView.Adapter<AttendeeFormViewHolder>() {
    private val attendeeForms = ArrayList<AttendeeForm>()

    fun addAll(forms: List<AttendeeForm>) {
        if (attendeeForms.isNotEmpty())
            this.attendeeForms.clear()
        this.attendeeForms.addAll(forms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeFormViewHolder {
        val attendeeFormView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_attendee_form, parent, false)
        return AttendeeFormViewHolder(attendeeFormView)
    }

    override fun onBindViewHolder(holder: AttendeeFormViewHolder, position: Int) {
        val attendeeForm = attendeeForms[position]
        holder.bind(attendeeForm)
    }

    override fun getItemCount(): Int {
        return attendeeForms.size
    }

}