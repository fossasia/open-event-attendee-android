package org.fossasia.openevent.general.attendees

import android.support.design.widget.TextInputLayout
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.item_attendee.view.*
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.ticket.TicketId

class AttendeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(attendeeRecyclerAdapter: AttendeeRecyclerAdapter, forms: List<CustomForm>, formVisibility: Boolean, position: Int) {
        setText(itemView.attendeeItemCountry, attendeeRecyclerAdapter.attendeeList[position].country)
        setText(itemView.attendeeItemLastName, attendeeRecyclerAdapter.attendeeList[position].lastname)
        setText(itemView.attendeeItemEmail, attendeeRecyclerAdapter.attendeeList[position].email)
        setText(itemView.attendeeItemFirstName, attendeeRecyclerAdapter.attendeeList[position].firstname)
        itemView.attendeeItemTicketName.text = attendeeRecyclerAdapter.ticketList[position].name
        fillInformationSection(forms, formVisibility)
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val id = attendeeRecyclerAdapter.attendeeList[position].id
                attendeeRecyclerAdapter.attendeeList.removeAt(position)
                val attendee = Attendee(id, firstname = itemView.attendeeItemFirstName.text.toString(),
                        lastname = itemView.attendeeItemLastName.text.toString(),
                        email = itemView.attendeeItemEmail.text.toString(),
                        country = itemView.attendeeItemCountry.text.toString(),
                        ticket = TicketId(attendeeRecyclerAdapter.ticketList[position].id.toLong()),
                        event = attendeeRecyclerAdapter.eventId)
                attendeeRecyclerAdapter.attendeeList.add(position, attendee)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }
        itemView.attendeeItemFirstName.addTextChangedListener(textWatcher)
        itemView.attendeeItemLastName.addTextChangedListener(textWatcher)
        itemView.attendeeItemEmail.addTextChangedListener(textWatcher)
        itemView.attendeeItemCountry.addTextChangedListener(textWatcher)
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