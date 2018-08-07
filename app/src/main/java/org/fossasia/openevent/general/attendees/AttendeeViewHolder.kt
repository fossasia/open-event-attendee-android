package org.fossasia.openevent.general.attendees

import android.support.design.widget.TextInputLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.item_attendee.view.*
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.ticket.Ticket

class AttendeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(attendeesAndTicket: Pair<Attendee, Ticket>, forms: List<CustomForm>, formVisibility: Boolean) {
        setText(itemView.attendeeItemCountry, attendeesAndTicket.first.country)
        setText(itemView.attendeeItemLastName, attendeesAndTicket.first.lastname)
        setText(itemView.attendeeItemEmail, attendeesAndTicket.first.email)
        setText(itemView.attendeeItemFirstName, attendeesAndTicket.first.firstname)
        itemView.attendeeItemTicketName.text = attendeesAndTicket.second.name
        fillInformationSection(forms, formVisibility)
    }

    fun setText(editText: EditText, string: String?) {
        if (!string.isNullOrEmpty())
            editText.setText(string)
    }

    private fun fillInformationSection(forms: List<CustomForm>, formsVisibility: Boolean) {
        val layout = itemView.attendeeInformation
        if (!formsVisibility) {
            layout.visibility == View.GONE
            return
        }
        for (form in forms) {
            if (form.type == "text") {
                val inputLayout = TextInputLayout(itemView.context)
                val editTextSection = EditText(itemView.context)
                editTextSection.hint = form.fieldIdentifier.capitalize()
                inputLayout.addView(editTextSection)
                inputLayout.setPadding(0, 0, 0, 20)
                layout.addView(inputLayout)
            }
        }
    }
}