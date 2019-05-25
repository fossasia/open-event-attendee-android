package org.fossasia.openevent.general.attendees

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.fragment_attendee.view.cvc
import kotlinx.android.synthetic.main.fragment_attendee.view.email
import kotlinx.android.synthetic.main.fragment_attendee.view.firstName
import kotlinx.android.synthetic.main.fragment_attendee.view.helloUser
import kotlinx.android.synthetic.main.fragment_attendee.view.lastName
import kotlinx.android.synthetic.main.fragment_attendee.view.postalCode
import kotlinx.android.synthetic.main.fragment_attendee.view.attendeeScrollView
import kotlinx.android.synthetic.main.fragment_attendee.view.accept
import kotlinx.android.synthetic.main.fragment_attendee.view.amount
import kotlinx.android.synthetic.main.fragment_attendee.view.attendeeInformation
import kotlinx.android.synthetic.main.fragment_attendee.view.attendeeRecycler
import kotlinx.android.synthetic.main.fragment_attendee.view.cardSelector
import kotlinx.android.synthetic.main.fragment_attendee.view.eventName
import kotlinx.android.synthetic.main.fragment_attendee.view.month
import kotlinx.android.synthetic.main.fragment_attendee.view.monthText
import kotlinx.android.synthetic.main.fragment_attendee.view.moreAttendeeInformation
import kotlinx.android.synthetic.main.fragment_attendee.view.paymentSelector
import kotlinx.android.synthetic.main.fragment_attendee.view.paymentSelectorContainer
import kotlinx.android.synthetic.main.fragment_attendee.view.progressBarAttendee
import kotlinx.android.synthetic.main.fragment_attendee.view.qty
import kotlinx.android.synthetic.main.fragment_attendee.view.register
import kotlinx.android.synthetic.main.fragment_attendee.view.selectCard
import kotlinx.android.synthetic.main.fragment_attendee.view.signOut
import kotlinx.android.synthetic.main.fragment_attendee.view.stripePayment
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketDetails
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketsRecycler
import kotlinx.android.synthetic.main.fragment_attendee.view.time
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketTableDetails
import kotlinx.android.synthetic.main.fragment_attendee.view.year
import kotlinx.android.synthetic.main.fragment_attendee.view.yearText
import kotlinx.android.synthetic.main.fragment_attendee.view.cardNumber
import kotlinx.android.synthetic.main.fragment_attendee.view.acceptCheckbox
import kotlinx.android.synthetic.main.fragment_attendee.view.countryPicker
import kotlinx.android.synthetic.main.fragment_attendee.view.countryPickerContainer
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.ticket.TicketDetailsRecyclerAdapter
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import java.util.Calendar
import java.util.Currency
import kotlin.collections.ArrayList

class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private val attendeeViewModel by viewModel<AttendeeViewModel>()
    private val ticketsRecyclerAdapter: TicketDetailsRecyclerAdapter = TicketDetailsRecyclerAdapter()
    private val attendeeRecyclerAdapter: AttendeeRecyclerAdapter = AttendeeRecyclerAdapter()
    private val safeArgs: AttendeeFragmentArgs by navArgs()

    private lateinit var API_KEY: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (attendeeViewModel.ticketIdAndQty == null) {
            attendeeViewModel.ticketIdAndQty = safeArgs.ticketIdAndQty?.value
            attendeeViewModel.singleTicket = safeArgs.ticketIdAndQty?.value?.map { it.second }?.sum() == 1
        }
        API_KEY = BuildConfig.STRIPE_API_KEY

        attendeeRecyclerAdapter.setEventId(safeArgs.eventId)
        if (attendeeViewModel.paymentCurrency.isNotBlank())
            ticketsRecyclerAdapter.setCurrency(attendeeViewModel.paymentCurrency)
        safeArgs.ticketIdAndQty?.value?.let {
            val quantities = it.map { pair -> pair.second }.filter { it != 0 }
            ticketsRecyclerAdapter.setQuantity(quantities)
            attendeeRecyclerAdapter.setQuantity(quantities)
        }
        attendeeViewModel.forms.value?.let { attendeeRecyclerAdapter.setCustomForm(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        setToolbar(activity, getString(R.string.attendee_details))
        setHasOptionsMenu(true)

        attendeeViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        attendeeViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBarAttendee.isVisible = it
                rootView.register.isEnabled = !it
            })

        setupEventInfo()
        setupTicketDetailTable()
        setupUser()
        setupAttendeeDetails()
        setupCustomForms()
        setupPaymentOptions()
        setupCountryOptions()
        setupCardNumber()
        setupCardType()
        setupMonthOptions()
        setupYearOptions()
        setupTermsAndCondition()
        setupRegisterOrder()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val attendeeDetailChangeListener = object : AttendeeDetailChangeListener {
            override fun onAttendeeDetailChanged(attendee: Attendee, position: Int) {
                attendeeViewModel.attendees[position] = attendee
            }
        }
        attendeeRecyclerAdapter.apply {
            attendeeChangeListener = attendeeDetailChangeListener
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkConnected(context)) {
            rootView.progressBarAttendee.isVisible = false
            rootView.attendeeScrollView.longSnackbar(getString(R.string.no_internet_connection_message))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        attendeeRecyclerAdapter.attendeeChangeListener = null
    }

    private fun setupEventInfo() {
        attendeeViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEventDetailsUI(it)
            })

        val currentEvent = attendeeViewModel.event.value
        if (currentEvent == null)
            attendeeViewModel.loadEvent(safeArgs.eventId)
        else
            loadEventDetailsUI(currentEvent)
    }

    private fun setupTicketDetailTable() {
        rootView.qty.text = " — ${attendeeViewModel.ticketIdAndQty?.map { it.second }?.sum()} items"
        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        rootView.ticketTableDetails.setOnClickListener {
            attendeeViewModel.ticketDetailsVisible = !attendeeViewModel.ticketDetailsVisible
            loadTicketDetailsTableUI(attendeeViewModel.ticketDetailsVisible)
        }
        loadTicketDetailsTableUI(attendeeViewModel.ticketDetailsVisible)

        attendeeViewModel.totalAmount
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.paymentSelectorContainer.visibility = if (it > 0) View.VISIBLE else View.GONE
                rootView.countryPickerContainer.visibility = if (it > 0) View.VISIBLE else View.GONE
                rootView.amount.text = "Total: ${attendeeViewModel.paymentCurrency}$it"
            })

        attendeeViewModel.tickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer { tickets ->
                ticketsRecyclerAdapter.addAll(tickets)
                if (!attendeeViewModel.singleTicket)
                    attendeeRecyclerAdapter.addAllTickets(tickets)
            })

        val currentTickets = attendeeViewModel.tickets.value
        val currentTotalPrice = attendeeViewModel.totalAmount.value
        if (currentTickets != null && currentTotalPrice != null) {
            rootView.paymentSelector.visibility = if (currentTotalPrice > 0) View.VISIBLE else View.GONE
            rootView.amount.text = "Total: ${attendeeViewModel.paymentCurrency}$currentTotalPrice"

            ticketsRecyclerAdapter.addAll(currentTickets)
            attendeeRecyclerAdapter.addAllTickets(currentTickets)
        } else {
            attendeeViewModel.getTickets()
        }
    }

    private fun setupUser() {
        attendeeViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer { user ->
                loadUserUI(user)
                if (attendeeViewModel.singleTicket) {
                    val pos = attendeeViewModel.ticketIdAndQty?.map { it.second }?.indexOf(1)
                    val ticket = pos?.let { it1 -> attendeeViewModel.ticketIdAndQty?.get(it1)?.first?.toLong() } ?: -1
                    val attendee = Attendee(id = attendeeViewModel.getId(),
                        firstname = rootView.firstName.text.toString(),
                        lastname = rootView.lastName.text.toString(),
                        city = getAttendeeField("city"),
                        address = getAttendeeField("address"),
                        state = getAttendeeField("state"),
                        email = rootView.email.text.toString(),
                        ticket = TicketId(ticket),
                        event = EventId(safeArgs.eventId))
                    attendeeViewModel.attendees.clear()
                    attendeeViewModel.attendees.add(attendee)
                }
            })

        rootView.signOut.setOnClickListener {
            AlertDialog.Builder(requireContext()).setMessage(getString(R.string.message))
                .setPositiveButton(getString(R.string.logout)) { _, _ ->
                    attendeeViewModel.logout()
                    activity?.onBackPressed()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                .show()
        }

        val currentUser = attendeeViewModel.user.value
        if (currentUser == null)
            attendeeViewModel.loadUser()
        else
            loadUserUI(currentUser)
    }

    private fun setupAttendeeDetails() {
        if (attendeeViewModel.singleTicket)
            rootView.attendeeRecycler.visibility = View.GONE
        rootView.attendeeRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.attendeeRecycler.adapter = attendeeRecyclerAdapter
        rootView.attendeeRecycler.isNestedScrollingEnabled = false

        if (attendeeViewModel.attendees.isEmpty()) {
            if (attendeeViewModel.singleTicket) {
                val pos = attendeeViewModel.ticketIdAndQty?.map { it.second }?.indexOf(1)
                val ticket = pos?.let { it1 -> attendeeViewModel.ticketIdAndQty?.get(it1)?.first?.toLong() } ?: -1
                val attendee = Attendee(id = attendeeViewModel.getId(),
                    firstname = rootView.firstName.text.toString(),
                    lastname = rootView.lastName.text.toString(),
                    city = getAttendeeField("city"),
                    address = getAttendeeField("address"),
                    state = getAttendeeField("state"),
                    email = rootView.email.text.toString(),
                    ticket = TicketId(ticket),
                    event = EventId(safeArgs.eventId))
                attendeeViewModel.attendees.add(attendee)
            } else {
                attendeeViewModel.ticketIdAndQty?.let {
                    it.forEach { pair ->
                        repeat(pair.second) {
                            attendeeViewModel.attendees.add(Attendee(
                                id = attendeeViewModel.getId(),
                                firstname = "", lastname = "", city = getAttendeeField("city"),
                                address = getAttendeeField("address"), state = getAttendeeField("state"),
                                email = "", ticket = TicketId(pair.first.toLong()), event = EventId(safeArgs.eventId)
                            ))
                        }
                    }
                }
            }
        }
        attendeeRecyclerAdapter.addAllAttendees(attendeeViewModel.attendees)
    }

    private fun setupCustomForms() {
        attendeeViewModel.forms
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (attendeeViewModel.singleTicket) {
                    fillInformationSection(it)
                    if (it.isNotEmpty()) {
                        rootView.moreAttendeeInformation.visibility = View.VISIBLE
                    }
                }
                attendeeRecyclerAdapter.setCustomForm(it)
            })

        val currentForms = attendeeViewModel.forms.value
        if (currentForms == null) {
            attendeeViewModel.getCustomFormsForAttendees(safeArgs.eventId)
        } else {
            if (attendeeViewModel.singleTicket) {
                fillInformationSection(currentForms)
                if (currentForms.isNotEmpty()) {
                    rootView.moreAttendeeInformation.visibility = View.VISIBLE
                }
            }
            attendeeRecyclerAdapter.setCustomForm(currentForms)
        }
    }

    private fun setupCountryOptions() {
        ArrayAdapter.createFromResource(
            requireContext(), R.array.country_arrays,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            rootView.countryPicker.adapter = adapter
            if (attendeeViewModel.countryPosition == -1)
                autoSetCurrentCountry()
            else
                rootView.countryPicker.setSelection(attendeeViewModel.countryPosition)
        }
        rootView.countryPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /*Do nothing*/ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                attendeeViewModel.countryPosition = position
            }
        }
        rootView.countryPickerContainer.visibility = if (attendeeViewModel.singleTicket) View.VISIBLE else View.GONE
    }

    private fun setupPaymentOptions() {
        val paymentOptions = ArrayList<String>()
        paymentOptions.add(getString(R.string.paypal))
        paymentOptions.add(getString(R.string.stripe))
        rootView.paymentSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            paymentOptions)
        rootView.paymentSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { /*Do nothing*/ }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                attendeeViewModel.selectedPaymentOption = position
                if (position == paymentOptions.indexOf(getString(R.string.stripe)))
                    rootView.stripePayment.visibility = View.VISIBLE
                else
                    rootView.stripePayment.visibility = View.GONE
            }
        }
        if (attendeeViewModel.selectedPaymentOption != -1)
            rootView.paymentSelector.setSelection(attendeeViewModel.selectedPaymentOption)
    }

    private fun setupCardNumber() {
        rootView.cardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /*Do Nothing*/ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null || s.length < 3) {
                    setCardSelectorAndError(0, visibility = true, error = false)
                    return
                }
                val card = Utils.getCardType(s.toString())
                if (card == Utils.cardType.NONE) {
                    setCardSelectorAndError(0, visibility = true, error = true)
                    return
                }

                val pos: Int = card.let {
                    when (it) {
                        Utils.cardType.AMERICAN_EXPRESS -> 1
                        Utils.cardType.MASTER_CARD -> 2
                        Utils.cardType.VISA -> 3
                        Utils.cardType.DISCOVER -> 4
                        Utils.cardType.DINERS_CLUB -> 5
                        Utils.cardType.UNIONPAY -> 6
                        else -> 0
                    }
                }
                setCardSelectorAndError(pos, visibility = false, error = false)
            }

            private fun setCardSelectorAndError(pos: Int, visibility: Boolean, error: Boolean) {
                rootView.cardSelector.setSelection(pos, true)
                rootView.cardSelector.isVisible = visibility
                if (error) {
                    rootView.cardNumber.error = "Invalid card number"
                    return
                }
                rootView.cardNumber.error = null
            }
        })
    }

    private fun setupMonthOptions() {
        val month = ArrayList<String>()
        month.add(getString(R.string.month_string))
        month.add(getString(R.string.january))
        month.add(getString(R.string.february))
        month.add(getString(R.string.march))
        month.add(getString(R.string.april))
        month.add(getString(R.string.may))
        month.add(getString(R.string.june))
        month.add(getString(R.string.july))
        month.add(getString(R.string.august))
        month.add(getString(R.string.september))
        month.add(getString(R.string.october))
        month.add(getString(R.string.november))
        month.add(getString(R.string.december))

        rootView.month.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            month)
        rootView.month.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { /* Do nothing */ }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                attendeeViewModel.monthSelectedPosition = p2
                rootView.monthText.text = month[p2]
            }
        }
        rootView.monthText.setOnClickListener {
            rootView.month.performClick()
        }

        rootView.month.setSelection(attendeeViewModel.monthSelectedPosition)
    }

    private fun setupYearOptions() {
        val year = ArrayList<String>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        year.add(getString(R.string.year_string))
        val a = currentYear + 20
        for (i in currentYear..a) {
            year.add(i.toString())
        }

        rootView.year.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            year)
        rootView.year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { /* Do nothing */ }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                attendeeViewModel.yearSelectedPosition = pos
                rootView.yearText.text = year[pos]
            }
        }
        rootView.yearText.setOnClickListener {
            rootView.year.performClick()
        }

        rootView.year.setSelection(attendeeViewModel.yearSelectedPosition)
    }

    private fun setupCardType() {
        val cardType = ArrayList<String>()
        cardType.add(getString(R.string.select_card))
        cardType.add(getString(R.string.american_express_pay_message))
        cardType.add(getString(R.string.mastercard_pay_message))
        cardType.add(getString(R.string.visa_pay_message))
        cardType.add(getString(R.string.discover_pay_message))
        cardType.add(getString(R.string.diners_pay_message))
        cardType.add(getString(R.string.unionpay_pay_message))

        rootView.cardSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            cardType)
        rootView.cardSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { /* Do nothing */ }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                attendeeViewModel.cardTypePosition = pos
                rootView.selectCard.text = cardType[pos]
            }
        }

        rootView.cardSelector.setSelection(attendeeViewModel.cardTypePosition)
    }

    private fun setupTermsAndCondition() {
        val paragraph = SpannableStringBuilder()
        val startText = getString(R.string.start_text)
        val termsText = getString(R.string.terms_text)
        val middleText = getString(R.string.middle_text)
        val privacyText = getString(R.string.privacy_text)

        paragraph.append(startText)
        paragraph.append(" $termsText")
        paragraph.append(" $middleText")
        paragraph.append(" $privacyText")

        val termsSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(requireContext(), getString(R.string.terms_of_service))
            }
        }

        val privacyPolicySpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(requireContext(), getString(R.string.privacy_policy))
            }
        }

        paragraph.setSpan(termsSpan, startText.length, startText.length + termsText.length + 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        paragraph.setSpan(privacyPolicySpan, paragraph.length - privacyText.length, paragraph.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // -1 so that we don't include "." in the link

        rootView.accept.text = paragraph
        rootView.accept.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupRegisterOrder() {
        rootView.register.setOnClickListener {
            if (!isNetworkConnected(context)) {
                rootView.attendeeScrollView.longSnackbar(getString(R.string.no_internet_connection_message))
                return@setOnClickListener
            }
            if (!rootView.acceptCheckbox.isChecked) {
                rootView.attendeeScrollView.longSnackbar(getString(R.string.term_and_conditions))
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.confirmation_dialog)

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val attendees = attendeeViewModel.attendees

                if (attendeeViewModel.areAttendeeEmailsValid(attendees)) {
                    val country = rootView.countryPicker.selectedItem.toString()
                    val paymentOption = rootView.paymentSelector.selectedItem.toString()
                    attendeeViewModel.createAttendees(attendees, country, paymentOption)
                } else {
                    rootView.attendeeScrollView.longSnackbar(getString(R.string.invalid_email_address_message))
                }
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                rootView.snackbar(getString(R.string.order_not_completed))
            }
            builder.show()
        }

        attendeeViewModel.isAttendeeCreated.observe(viewLifecycleOwner, Observer { isAttendeeCreated ->
            if (isAttendeeCreated &&
                rootView.paymentSelector.selectedItem.toString() == getString(R.string.stripe)) {
                sendToken()
            }
        })

        attendeeViewModel.ticketSoldOut
            .nonNull()
            .observe(this, Observer {
                showTicketSoldOutDialog(it)
            })

        attendeeViewModel.paymentCompleted
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    openOrderCompletedFragment()
            })
    }

    private fun showTicketSoldOutDialog(show: Boolean) {
        if (show) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(getString(R.string.tickets_sold_out))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }

    private fun sendToken() {
        val card = Card(rootView.cardNumber.text.toString(), attendeeViewModel.monthSelectedPosition,
            attendeeViewModel.yearSelectedPosition, rootView.cvc.text.toString())
        card.addressCountry = rootView.countryPicker.selectedItem.toString()
        card.addressZip = rootView.postalCode.text.toString()

        if (card.brand != null && card.brand != "Unknown")
            rootView.selectCard.text = "Pay by ${card.brand}"

        val validDetails: Boolean? = card.validateCard()
        if (validDetails != null && !validDetails)
            rootView.snackbar(getString(R.string.invalid_card_data_message))
        else
            Stripe(requireContext())
                .createToken(card, API_KEY, object : TokenCallback {
                    override fun onSuccess(token: Token) {
                        // Send this token to server
                        val charge = Charge(attendeeViewModel.getId().toInt(), token.id, null)
                        attendeeViewModel.completeOrder(charge)
                    }

                    override fun onError(error: Exception) {
                        rootView.snackbar(error.localizedMessage.toString())
                    }
                })
    }

    private fun loadEventDetailsUI(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)
        attendeeViewModel.paymentCurrency = Currency.getInstance(event.paymentCurrency).symbol
        ticketsRecyclerAdapter.setCurrency(attendeeViewModel.paymentCurrency)

        rootView.eventName.text = "${event.name} - ${EventUtils.getFormattedDate(startsAt)}"
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
            .append(" - ")
            .append(EventUtils.getFormattedDate(endsAt))
            .append(" • ")
            .append(EventUtils.getFormattedTime(startsAt))
    }

    private fun loadUserUI(user: User) {
        rootView.helloUser.text = "Hello ${user.firstName.nullToEmpty()}"
        rootView.firstName.text = Editable.Factory.getInstance().newEditable(user.firstName.nullToEmpty())
        rootView.lastName.text = Editable.Factory.getInstance().newEditable(user.lastName.nullToEmpty())
        rootView.email.text = Editable.Factory.getInstance().newEditable(user.email.nullToEmpty())
    }

    private fun loadTicketDetailsTableUI(show: Boolean) {
        if (show) {
            rootView.ticketDetails.visibility = View.VISIBLE
            rootView.ticketTableDetails.text = context?.getString(R.string.hide)
        } else {
            rootView.ticketDetails.visibility = View.GONE
            rootView.ticketTableDetails.text = context?.getString(R.string.view)
        }
    }

    private fun openOrderCompletedFragment() {
        attendeeViewModel.paymentCompleted.value = false
        findNavController(rootView).navigate(AttendeeFragmentDirections
            .actionAttendeeToOrderCompleted(safeArgs.eventId))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fillInformationSection(forms: List<CustomForm>) {
        val layout = rootView.attendeeInformation
        for (form in forms) {
            if (form.type == "text") {
                val inputLayout = TextInputLayout(context)
                val editTextSection = EditText(context)
                editTextSection.hint = form.fieldIdentifier.capitalize()
                inputLayout.addView(editTextSection)
                inputLayout.setPadding(0, 0, 0, 20)
                layout.addView(inputLayout)
                attendeeViewModel.identifierList.add(form.fieldIdentifier)
                attendeeViewModel.editTextList.add(editTextSection)
            }
        }
    }

    private fun getAttendeeField(identifier: String): String {
        val index = attendeeViewModel.identifierList.indexOf(identifier)
        return if (index == -1) "" else index.let { attendeeViewModel.editTextList[it] }.text.toString()
    }

    private fun autoSetCurrentCountry() {
        val telephonyManager: TelephonyManager = activity?.getSystemService(Context.TELEPHONY_SERVICE)
            as TelephonyManager
        val currentCountryCode = telephonyManager.networkCountryIso
        val countryCodes = resources.getStringArray(R.array.country_code_arrays)
        val countryIndex = countryCodes.indexOf(currentCountryCode.toUpperCase())
        if (countryIndex != -1) rootView.countryPicker.setSelection(countryIndex)
    }
}
