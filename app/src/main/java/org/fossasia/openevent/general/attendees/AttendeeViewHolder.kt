package org.fossasia.openevent.general.attendees

import com.google.android.material.textfield.TextInputLayout
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.item_attendee.view.*
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.checkEmpty
import org.fossasia.openevent.general.utils.checkValidEmail
import org.fossasia.openevent.general.utils.setRequired

class AttendeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var identifierList = ArrayList<String>()
    private var editTextList = ArrayList<EditText>()
    var onAttendeeDetailChanged: AttendeeDetailChangeListener? = null

    fun bind(attendee: Attendee, ticket: Ticket, customForm: List<CustomForm>, position: Int, eventId: Long) {

        itemView.itemEmailLayout.setRequired()
        itemView.itemFirstNameLayout.setRequired()
        itemView.itemLastNameLayout.setRequired()
        itemView.attendeeItemCountry.setText(attendee.country)
        itemView.attendeeItemLastName.setText(attendee.lastname)
        itemView.attendeeItemEmail.setText(attendee.email)
        itemView.attendeeItemFirstName.setText(attendee.firstname)
        itemView.attendeeItemTicketName.text = "Ticket Name - ${ticket.name}"

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newAttendee = Attendee(
                    id = attendee.id,
                    firstname = itemView.attendeeItemFirstName.text.toString(),
                    lastname = itemView.attendeeItemLastName.text.toString(),
                    email = itemView.attendeeItemEmail.text.toString(),
                    city = getAttendeeField("city"),
                    address = getAttendeeField("address"),
                    state = getAttendeeField("state"),
                    country = itemView.attendeeItemCountry.text.toString(),
                    ticket = TicketId(ticket.id.toLong()),
                    event = EventId(eventId))
                onAttendeeDetailChanged?.onAttendeeDetailChanged(newAttendee, position)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do nothing*/ }
        }
        itemView.attendeeItemFirstName.addTextChangedListener(textWatcher)
        itemView.attendeeItemLastName.addTextChangedListener(textWatcher)
        itemView.attendeeItemEmail.addTextChangedListener(textWatcher)
        itemView.attendeeItemCountry.addTextChangedListener(textWatcher)
        fillInformationSection(customForm, textWatcher)

        if (customForm.isEmpty()) itemView.moreAttendeeInformation.visibility = View.GONE
        val price = ticket.price
        if ((price != null && price.equals(0.toFloat())) || price == null) {
            itemView.countryArea.visibility = View.GONE
        }
    }

    fun checkValidFields(): Boolean =
        itemView.attendeeItemFirstName.checkEmpty() && itemView.attendeeItemLastName.checkEmpty() &&
            itemView.attendeeItemEmail.checkEmpty() && itemView.attendeeItemEmail.checkValidEmail()

    private fun fillInformationSection(forms: List<CustomForm>, textWatcher: TextWatcher) {
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

    private fun getAttendeeField(identifier: String): String {
        val index = identifierList.indexOf(identifier)
        return if (index == -1) "" else index.let { editTextList[it] }.text.toString()
    }
}
