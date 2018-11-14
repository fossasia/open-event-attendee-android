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
    private var identifierList = ArrayList<String>()
    private var editTextList = ArrayList<EditText>()
    lateinit var textWatcher: TextWatcher

    fun bind(attendeeRecyclerAdapter: AttendeeRecyclerAdapter, position: Int) {

        setText(itemView.attendeeItemCountry, attendeeRecyclerAdapter.attendeeList[position].country)
        setText(itemView.attendeeItemLastName, attendeeRecyclerAdapter.attendeeList[position].lastname)
        setText(itemView.attendeeItemEmail, attendeeRecyclerAdapter.attendeeList[position].email)
        setText(itemView.attendeeItemFirstName, attendeeRecyclerAdapter.attendeeList[position].firstname)
        itemView.attendeeItemTicketName.text = "Ticket Name - ${attendeeRecyclerAdapter.ticketList[position].name}"

        textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val id = attendeeRecyclerAdapter.attendeeList[position].id
                attendeeRecyclerAdapter.attendeeList.removeAt(position)
                val attendee = Attendee(id, firstname = itemView.attendeeItemFirstName.text.toString(),
                        lastname = itemView.attendeeItemLastName.text.toString(),
                        email = itemView.attendeeItemEmail.text.toString(),
                        city = getAttendeeField("city"),
                        address = getAttendeeField("address"),
                        state = getAttendeeField("state"),
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

        fillInformationSection(attendeeRecyclerAdapter.customForm)
        if (attendeeRecyclerAdapter.customForm.isEmpty()) itemView.moreAttendeeInformation.visibility = View.GONE
        val price = attendeeRecyclerAdapter.ticketList[position].price
        if ((price != null && price.equals(0.toFloat())) || price == null) {
            itemView.countryArea.visibility = View.GONE
        }
    }

    fun setText(editText: EditText, string: String?) {
        if (!string.isNullOrEmpty())
            editText.setText(string)
    }

    private fun fillInformationSection(forms: List<CustomForm>) {
        val layout = itemView.attendeeInformation
        for (form in forms) {
            if (form.type == "text") {
                val inputLayout = TextInputLayout(itemView.context)
                val editTextSection = EditText(itemView.context)
                editTextSection.addTextChangedListener(textWatcher)
                editTextSection.hint = form.fieldIdentifier.capitalize()
                inputLayout.addView(editTextSection)
                inputLayout.setPadding(0, 0, 0, 20)
                layout.addView(inputLayout)
                identifierList.add(form.fieldIdentifier)
                editTextList.add(editTextSection)
            }
        }
    }

    fun getAttendeeField(identifier: String): String {
        val index = identifierList.indexOf(identifier)
        return if (index == -1) "" else index.let { editTextList[it] }.text.toString()
    }
}
