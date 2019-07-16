package org.fossasia.openevent.general.attendees

import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.item_attendee.view.firstNameLayout
import kotlinx.android.synthetic.main.item_attendee.view.firstName
import kotlinx.android.synthetic.main.item_attendee.view.lastNameLayout
import kotlinx.android.synthetic.main.item_attendee.view.lastName
import kotlinx.android.synthetic.main.item_attendee.view.emailLayout
import kotlinx.android.synthetic.main.item_attendee.view.email
import kotlinx.android.synthetic.main.item_attendee.view.attendeeBillingAddressLayout
import kotlinx.android.synthetic.main.item_attendee.view.attendeeBillingAddress
import kotlinx.android.synthetic.main.item_attendee.view.phoneLayout
import kotlinx.android.synthetic.main.item_attendee.view.phone
import kotlinx.android.synthetic.main.item_attendee.view.workPhoneLayout
import kotlinx.android.synthetic.main.item_attendee.view.workPhone
import kotlinx.android.synthetic.main.item_attendee.view.addressLayout
import kotlinx.android.synthetic.main.item_attendee.view.address
import kotlinx.android.synthetic.main.item_attendee.view.workAddressLayout
import kotlinx.android.synthetic.main.item_attendee.view.workAddress
import kotlinx.android.synthetic.main.item_attendee.view.blogLayout
import kotlinx.android.synthetic.main.item_attendee.view.blog
import kotlinx.android.synthetic.main.item_attendee.view.websiteLayout
import kotlinx.android.synthetic.main.item_attendee.view.website
import kotlinx.android.synthetic.main.item_attendee.view.twitterLayout
import kotlinx.android.synthetic.main.item_attendee.view.twitter
import kotlinx.android.synthetic.main.item_attendee.view.facebookLayout
import kotlinx.android.synthetic.main.item_attendee.view.facebook
import kotlinx.android.synthetic.main.item_attendee.view.githubLayout
import kotlinx.android.synthetic.main.item_attendee.view.github
import kotlinx.android.synthetic.main.item_attendee.view.shippingAddressLayout
import kotlinx.android.synthetic.main.item_attendee.view.shippingAddress
import kotlinx.android.synthetic.main.item_attendee.view.taxBusinessInfoLayout
import kotlinx.android.synthetic.main.item_attendee.view.taxBusinessInfo
import kotlinx.android.synthetic.main.item_attendee.view.stateLayout
import kotlinx.android.synthetic.main.item_attendee.view.state
import kotlinx.android.synthetic.main.item_attendee.view.homeAddressLayout
import kotlinx.android.synthetic.main.item_attendee.view.homeAddress
import kotlinx.android.synthetic.main.item_attendee.view.cityLayout
import kotlinx.android.synthetic.main.item_attendee.view.city
import kotlinx.android.synthetic.main.item_attendee.view.genderLayout
import kotlinx.android.synthetic.main.item_attendee.view.genderText
import kotlinx.android.synthetic.main.item_attendee.view.genderSpinner
import kotlinx.android.synthetic.main.item_attendee.view.company
import kotlinx.android.synthetic.main.item_attendee.view.companyLayout
import kotlinx.android.synthetic.main.item_attendee.view.countryLayout
import kotlinx.android.synthetic.main.item_attendee.view.country
import kotlinx.android.synthetic.main.item_attendee.view.jobTitleLayout
import kotlinx.android.synthetic.main.item_attendee.view.jobTitle
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.attendees.forms.FormIdentifier
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.databinding.ItemAttendeeBinding
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.checkEmpty
import org.fossasia.openevent.general.utils.checkValidEmail
import org.fossasia.openevent.general.utils.checkValidURI
import org.fossasia.openevent.general.utils.emptyToNull
import org.fossasia.openevent.general.utils.setRequired
import org.fossasia.openevent.general.utils.nullToEmpty

class AttendeeViewHolder(private val binding: ItemAttendeeBinding) : RecyclerView.ViewHolder(binding.root) {
    private val resource = Resource()
    private val requiredList = mutableListOf<TextInputEditText>()
    var onAttendeeDetailChanged: AttendeeDetailChangeListener? = null

    fun bind(
        attendee: Attendee,
        ticket: Ticket,
        customForm: List<CustomForm>,
        position: Int,
        eventId: Long,
        firstAttendee: Attendee?
    ) {
        with(binding) {
            this.attendee = attendee
            this.ticket = ticket
        }

        if (position == 0) {
            if (firstAttendee != null) {
                itemView.firstName.text = SpannableStringBuilder(firstAttendee.firstname.nullToEmpty())
                itemView.lastName.text = SpannableStringBuilder(firstAttendee.lastname.nullToEmpty())
                itemView.email.text = SpannableStringBuilder(firstAttendee.email.nullToEmpty())
                setFieldEditable(false)
            } else {
                itemView.firstName.text = SpannableStringBuilder("")
                itemView.lastName.text = SpannableStringBuilder("")
                itemView.email.text = SpannableStringBuilder("")
                setFieldEditable(true)
            }
        } else {
            itemView.firstName.setText(attendee.firstname)
            itemView.lastName.setText(attendee.lastname)
            itemView.email.setText(attendee.email)
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newAttendee = getAttendeeInformation(attendee.id, ticket, eventId)
                onAttendeeDetailChanged?.onAttendeeDetailChanged(newAttendee, position)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do nothing*/ }
        }
        requiredList.clear()
        setupGendersSpinner(attendee, ticket, eventId, position)
        customForm.forEach { form ->
            setupCustomFormWithFields(form, textWatcher)
        }
    }
    private fun setupCustomFormWithFields(form: CustomForm, textWatcher: TextWatcher) {
        when (form.fieldIdentifier) {
            FormIdentifier.FIRST_NAME ->
                setupField(itemView.firstNameLayout, itemView.firstName, form.isRequired, textWatcher)
            FormIdentifier.LAST_NAME ->
                setupField(itemView.lastNameLayout, itemView.lastName, form.isRequired, textWatcher)
            FormIdentifier.EMAIL ->
                setupField(itemView.emailLayout, itemView.email, form.isRequired, textWatcher)
            FormIdentifier.ADDRESS ->
                setupField(itemView.addressLayout, itemView.address, form.isRequired, textWatcher)
            FormIdentifier.CITY ->
                setupField(itemView.cityLayout, itemView.city, form.isRequired, textWatcher)
            FormIdentifier.STATE ->
                setupField(itemView.stateLayout, itemView.state, form.isRequired, textWatcher)
            FormIdentifier.COUNTRY ->
                setupField(itemView.countryLayout, itemView.country, form.isRequired, textWatcher)
            FormIdentifier.JOB_TITLE ->
                setupField(itemView.jobTitleLayout, itemView.jobTitle, form.isRequired, textWatcher)
            FormIdentifier.PHONE ->
                setupField(itemView.phoneLayout, itemView.phone, form.isRequired, textWatcher)
            FormIdentifier.TAX_INFO ->
                setupField(itemView.taxBusinessInfoLayout, itemView.taxBusinessInfo, form.isRequired, textWatcher)
            FormIdentifier.BILLING_ADDRESS ->
                setupField(itemView.attendeeBillingAddressLayout, itemView.attendeeBillingAddress, form.isRequired,
                    textWatcher)
            FormIdentifier.HOME_ADDRESS ->
                setupField(itemView.homeAddressLayout, itemView.homeAddress, form.isRequired, textWatcher)
            FormIdentifier.SHIPPING_ADDRESS ->
                setupField(itemView.shippingAddressLayout, itemView.shippingAddress, form.isRequired, textWatcher)
            FormIdentifier.WORK_ADDRESS ->
                setupField(itemView.workAddressLayout, itemView.workAddress, form.isRequired, textWatcher)
            FormIdentifier.WORK_PHONE ->
                setupField(itemView.workPhoneLayout, itemView.workPhone, form.isRequired, textWatcher)
            FormIdentifier.WEBSITE ->
                setupField(itemView.websiteLayout, itemView.website, form.isRequired, textWatcher)
            FormIdentifier.BLOG ->
                setupField(itemView.blogLayout, itemView.blog, form.isRequired, textWatcher)
            FormIdentifier.TWITTER ->
                setupField(itemView.twitterLayout, itemView.twitter, form.isRequired, textWatcher)
            FormIdentifier.FACEBOOK ->
                setupField(itemView.facebookLayout, itemView.facebook, form.isRequired, textWatcher)
            FormIdentifier.COMPANY ->
                setupField(itemView.companyLayout, itemView.company, form.isRequired, textWatcher)
            FormIdentifier.GITHUB ->
                setupField(itemView.githubLayout, itemView.github, form.isRequired, textWatcher)
            FormIdentifier.GENDER -> {
                itemView.genderLayout.isVisible = true
                if (form.isRequired) {
                    itemView.genderText.text = "${resource.getString(R.string.gender)}*"
                }
            }
            else -> return
        }
    }

    private fun setupGendersSpinner(attendee: Attendee, ticket: Ticket, eventId: Long, position: Int) {
        val genders = mutableListOf(resource.getString(R.string.male),
            resource.getString(R.string.female), resource.getString(R.string.others))
        itemView.genderSpinner.adapter =
            ArrayAdapter(itemView.context, android.R.layout.simple_spinner_dropdown_item, genders)

        val genderSelected = genders.indexOf(attendee.gender)
        if (genderSelected != -1)
            itemView.genderSpinner.setSelection(genderSelected)

        itemView.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Do Nothing */ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, p: Int, id: Long) {
                val newAttendee = getAttendeeInformation(attendee.id, ticket, eventId)
                onAttendeeDetailChanged?.onAttendeeDetailChanged(newAttendee, position)
            }
        }
    }

    private fun setupField(
        layout: TextInputLayout,
        editText: TextInputEditText,
        isRequired: Boolean,
        textWatcher: TextWatcher
    ) {
        layout.isVisible = true
        editText.addTextChangedListener(textWatcher)
        if (isRequired) {
            layout.setRequired()
            requiredList.add(editText)
        }
    }

    private fun setFieldEditable(editable: Boolean) {
        itemView.firstName.isEnabled = editable
        itemView.lastName.isEnabled = editable
        itemView.email.isEnabled = editable
    }

    fun checkValidFields(): Boolean {
        requiredList.forEach {
            if (!it.checkEmpty() ||
                (it.inputType == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS && !it.checkValidEmail()) ||
                (it.inputType == InputType.TYPE_TEXT_VARIATION_URI && !it.checkValidURI())) return false
        }
        return true
    }

    private fun getAttendeeInformation(id: Long, ticket: Ticket, eventId: Long): Attendee {
        return Attendee(
            id = id,
            firstname = itemView.firstName.text.toString(),
            lastname = itemView.lastName.text.toString(),
            email = itemView.email.text.toString(),
            address = itemView.address.text.toString().emptyToNull(),
            city = itemView.city.text.toString().emptyToNull(),
            state = itemView.state.text.toString().emptyToNull(),
            country = itemView.country.text.toString().emptyToNull(),
            jobTitle = itemView.jobTitle.text.toString().emptyToNull(),
            phone = itemView.phone.text.toString().emptyToNull(),
            taxBusinessInfo = itemView.taxBusinessInfo.text.toString().emptyToNull(),
            billingAddress = itemView.attendeeBillingAddress.text.toString().emptyToNull(),
            homeAddress = itemView.homeAddress.text.toString().emptyToNull(),
            shippingAddress = itemView.shippingAddress.text.toString().emptyToNull(),
            company = itemView.company.text.toString().emptyToNull(),
            workAddress = itemView.workAddress.text.toString().emptyToNull(),
            workPhone = itemView.workPhone.text.toString().emptyToNull(),
            website = itemView.website.text.toString().emptyToNull(),
            blog = itemView.blog.text.toString().emptyToNull(),
            twitter = itemView.twitter.text.toString().emptyToNull(),
            facebook = itemView.facebook.text.toString().emptyToNull(),
            github = itemView.github.text.toString().emptyToNull(),
            gender = itemView.genderSpinner.selectedItem.toString(),
            ticket = TicketId(ticket.id.toLong()),
            event = EventId(eventId))
    }
}
