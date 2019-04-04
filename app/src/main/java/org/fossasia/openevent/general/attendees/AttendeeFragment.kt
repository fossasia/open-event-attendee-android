package org.fossasia.openevent.general.attendees

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.fragment_attendee.cardNumber
import kotlinx.android.synthetic.main.fragment_attendee.cvc
import kotlinx.android.synthetic.main.fragment_attendee.email
import kotlinx.android.synthetic.main.fragment_attendee.firstName
import kotlinx.android.synthetic.main.fragment_attendee.helloUser
import kotlinx.android.synthetic.main.fragment_attendee.lastName
import kotlinx.android.synthetic.main.fragment_attendee.postalCode
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
import kotlinx.android.synthetic.main.fragment_attendee.view.progressBarAttendee
import kotlinx.android.synthetic.main.fragment_attendee.view.qty
import kotlinx.android.synthetic.main.fragment_attendee.view.register
import kotlinx.android.synthetic.main.fragment_attendee.view.selectCard
import kotlinx.android.synthetic.main.fragment_attendee.view.signOut
import kotlinx.android.synthetic.main.fragment_attendee.view.stripePayment
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketDetails
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketsRecycler
import kotlinx.android.synthetic.main.fragment_attendee.view.time
import kotlinx.android.synthetic.main.fragment_attendee.view.view
import kotlinx.android.synthetic.main.fragment_attendee.view.year
import kotlinx.android.synthetic.main.fragment_attendee.view.yearText
import kotlinx.android.synthetic.main.fragment_attendee.view.cardNumber
import kotlinx.android.synthetic.main.fragment_attendee.view.acceptCheckbox
import kotlinx.android.synthetic.main.fragment_attendee.view.countryPicker
import kotlinx.android.synthetic.main.fragment_attendee.view.countryPickerContainer
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.order.OrderCompletedFragmentArgs
import org.fossasia.openevent.general.ticket.TicketDetailsRecyclerAdapter
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Currency
import org.fossasia.openevent.general.utils.Utils.setToolbar

private const val STRIPE_KEY = "com.stripe.android.API_KEY"

class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private val attendeeViewModel by viewModel<AttendeeViewModel>()
    private val ticketsRecyclerAdapter: TicketDetailsRecyclerAdapter = TicketDetailsRecyclerAdapter()
    private val attendeeRecyclerAdapter: AttendeeRecyclerAdapter = AttendeeRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val safeArgs: AttendeeFragmentArgs by navArgs()

    private lateinit var eventId: EventId
    private var ticketIdAndQty: List<Pair<Int, Int>>? = null
    private var selectedPaymentOption: Int = -1
    private lateinit var paymentCurrency: String
    private var expiryMonth: Int = -1
    private var expiryYear: String? = null
    private var cardBrand: String? = null
    private lateinit var API_KEY: String
    private var singleTicket = false
    private var identifierList = ArrayList<String>()
    private var editTextList = ArrayList<EditText>()
    private var amount: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = EventId(safeArgs.eventId)
        ticketIdAndQty = safeArgs.ticketIdAndQty?.value
        singleTicket = ticketIdAndQty?.map { it.second }?.sum() == 1
        API_KEY = activity?.packageManager?.getApplicationInfo(activity?.packageName, PackageManager.GET_META_DATA)
            ?.metaData?.getString(STRIPE_KEY).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        setToolbar(activity, getString(R.string.attendee_details))
        setHasOptionsMenu(true)

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

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        rootView.attendeeRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.attendeeRecycler.adapter = attendeeRecyclerAdapter
        rootView.attendeeRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        attendeeViewModel.ticketDetails(ticketIdAndQty)

        attendeeViewModel.updatePaymentSelectorVisibility(ticketIdAndQty)
        val paymentOptions = ArrayList<String>()
        paymentOptions.add(getString(R.string.paypal))
        paymentOptions.add(getString(R.string.stripe))
        attendeeViewModel.paymentSelectorVisibility
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.paymentSelector.visibility = View.VISIBLE
                } else {
                    rootView.paymentSelector.visibility = View.GONE
                }
            })
        rootView.paymentSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            paymentOptions)
        rootView.paymentSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedPaymentOption = position
                if (position == paymentOptions.indexOf(getString(R.string.stripe)))
                    rootView.stripePayment.visibility = View.VISIBLE
                else
                    rootView.stripePayment.visibility = View.GONE
            }
        }

        attendeeViewModel.initializeSpinner()

        ArrayAdapter.createFromResource(
            requireContext(), R.array.country_arrays,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            rootView.countryPicker.adapter = adapter
            autoSetCurrentCountry()
        }

        rootView.cardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

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

            fun setCardSelectorAndError(pos: Int, visibility: Boolean, error: Boolean) {
                rootView.cardSelector.setSelection(pos, true)
                rootView.cardSelector.isVisible = visibility
                if (error) {
                    rootView.cardNumber.error = "Invalid card number"
                    return
                }
                rootView.cardNumber.error = null
            }
        })
        rootView.month.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            attendeeViewModel.month)
        rootView.month.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                expiryMonth = p2
                rootView.monthText.text = attendeeViewModel.month[p2]
            }
        }
        rootView.monthText.setOnClickListener {
            rootView.month.performClick()
        }

        rootView.year.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            attendeeViewModel.year)
        rootView.year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                expiryYear = attendeeViewModel.year[p2]
                if (expiryYear == "Year")
                    expiryYear = "2017" // invalid year, if the user hasn't selected the year
                rootView.yearText.text = attendeeViewModel.year[p2]
            }
        }
        rootView.yearText.setOnClickListener {
            rootView.year.performClick()
        }

        rootView.cardSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            attendeeViewModel.cardType)
        rootView.cardSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cardBrand = attendeeViewModel.cardType[p2]
                rootView.selectCard.text = cardBrand
            }
        }
        attendeeViewModel.qtyList
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                ticketsRecyclerAdapter.setQty(it)
            })

        rootView.view.setOnClickListener {
            val currentVisibility: Boolean = attendeeViewModel.ticketDetailsVisibility.value ?: false
            if (rootView.view.text == context?.getString(R.string.view)) {
                rootView.ticketDetails.visibility = View.VISIBLE
                rootView.view.text = context?.getString(R.string.hide)
            } else {
                attendeeViewModel.ticketDetailsVisibility.value = !currentVisibility
                rootView.ticketDetails.visibility = View.GONE
                rootView.view.text = context?.getString(R.string.view)
            }
        }

        attendeeViewModel.ticketDetailsVisibility
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.view.text = if (it) getString(R.string.hide) else getString(R.string.view)
                rootView.ticketDetails.visibility = if (it) View.VISIBLE else View.GONE
            })

        attendeeViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView, it, Snackbar.LENGTH_LONG).show()
            })

        attendeeViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBarAttendee.isVisible = it
                rootView.register.isEnabled = !it
            })

        attendeeViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEventDetails(it)
            })

        attendeeViewModel.totalAmount
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                amount = it
            })

        attendeeRecyclerAdapter.eventId = eventId
        attendeeViewModel.tickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer { tickets ->
                ticketsRecyclerAdapter.addAll(tickets)
                ticketsRecyclerAdapter.notifyDataSetChanged()
                if (!singleTicket)
                    tickets.forEach { ticket ->
                        val pos = ticketIdAndQty?.map { it.first }?.indexOf(ticket.id)
                        val iterations = pos?.let { ticketIdAndQty?.get(it)?.second } ?: 0
                        for (i in 0 until iterations)
                            attendeeRecyclerAdapter.add(Attendee(attendeeViewModel.getId()), ticket)
                        attendeeRecyclerAdapter.notifyDataSetChanged()
                    }
            })

        attendeeViewModel.totalQty
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.qty.text = " — $it items"
            })

        attendeeViewModel.countryVisibility
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (singleTicket) {
                    rootView.countryPickerContainer.visibility = if (it) View.VISIBLE else View.GONE
                }
            })

        attendeeViewModel.paymentCompleted
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    openOrderCompletedFragment()
            })

        attendeeViewModel.loadUser()
        attendeeViewModel.loadEvent(safeArgs.eventId)

        attendeeViewModel.attendee
            .nonNull()
            .observe(viewLifecycleOwner, Observer { user ->
                helloUser.text = "Hello ${user.firstName.nullToEmpty()}"
                firstName.text = Editable.Factory.getInstance().newEditable(user.firstName.nullToEmpty())
                lastName.text = Editable.Factory.getInstance().newEditable(user.lastName.nullToEmpty())
                email.text = Editable.Factory.getInstance().newEditable(user.email.nullToEmpty())
            })

        rootView.signOut.setOnClickListener {
            AlertDialog.Builder(requireContext()).setMessage(resources.getString(R.string.message))
                .setPositiveButton(resources.getString(R.string.logout)) { _, _ ->
                    attendeeViewModel.logout()
                    activity?.onBackPressed()
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                .show()
        }

        attendeeViewModel.getCustomFormsForAttendees(eventId.id)

        attendeeViewModel.forms
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (singleTicket)
                    fillInformationSection(it)
                attendeeRecyclerAdapter.setCustomForm(it)
                if (singleTicket)
                    if (!it.isEmpty()) {
                        rootView.moreAttendeeInformation.visibility = View.VISIBLE
                    }
                attendeeRecyclerAdapter.notifyDataSetChanged()
            })

        rootView.register.setOnClickListener {
            if (!isNetworkConnected(context)) {
                Snackbar.make(rootView.attendeeScrollView, getString(R.string.no_internet_connection_message),
                    Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!rootView.acceptCheckbox.isChecked) {
                Snackbar.make(rootView.attendeeScrollView,
                    getString(R.string.term_and_conditions), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.confirmation_dialog)

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val attendees = ArrayList<Attendee>()
                if (singleTicket) {
                    val pos = ticketIdAndQty?.map { it.second }?.indexOf(1)
                    val ticket = pos?.let { it1 -> ticketIdAndQty?.get(it1)?.first?.toLong() } ?: -1
                    val attendee = Attendee(id = attendeeViewModel.getId(),
                        firstname = firstName.text.toString(),
                        lastname = lastName.text.toString(),
                        city = getAttendeeField("city"),
                        address = getAttendeeField("address"),
                        state = getAttendeeField("state"),
                        email = email.text.toString(),
                        ticket = TicketId(ticket),
                        event = eventId)
                    attendees.add(attendee)
                } else {
                    attendees.addAll(attendeeRecyclerAdapter.attendeeList)
                }

                if (attendeeViewModel.areAttendeeEmailsValid(attendees)) {
                    val country = rootView.countryPicker.selectedItem.toString()
                    attendeeViewModel.createAttendees(attendees, country, paymentOptions[selectedPaymentOption])

                    attendeeViewModel.isAttendeeCreated.observe(viewLifecycleOwner, Observer { isAttendeeCreated ->
                        if (isAttendeeCreated && selectedPaymentOption ==
                            paymentOptions.indexOf(getString(R.string.stripe))) {
                            sendToken()
                        }
                    })
                } else {
                    Snackbar.make(rootView.attendeeScrollView,
                        getString(R.string.invalid_email_address_message), Snackbar.LENGTH_LONG).show()
                }
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                Snackbar.make(rootView, R.string.order_not_completed, Snackbar.LENGTH_SHORT).show()
            }
            builder.show()

            attendeeViewModel.ticketSoldOut
                .nonNull()
                .observe(this, Observer {
                    showTicketSoldOutDialog(it)
                })
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkConnected(context)) {
            rootView.progressBarAttendee.isVisible = false
            Snackbar.make(rootView.attendeeScrollView, "No internet connection!", Snackbar.LENGTH_LONG).show()
        }
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
        val card = Card(cardNumber.text.toString(), expiryMonth, expiryYear?.toInt(), cvc.text.toString())
        card.addressCountry = rootView.countryPicker.selectedItem.toString()
        card.addressZip = postalCode.text.toString()

        if (card.brand != null && card.brand != "Unknown")
            rootView.selectCard.text = "Pay by ${card.brand}"

        val validDetails: Boolean? = card.validateCard()
        if (validDetails != null && !validDetails)
            Snackbar.make(
                rootView, getString(R.string.invalid_card_data_message), Snackbar.LENGTH_SHORT
            ).show()
        else
            Stripe(requireContext())
                .createToken(card, API_KEY, object : TokenCallback {
                    override fun onSuccess(token: Token) {
                        // Send this token to server
                        val charge = Charge(attendeeViewModel.getId().toInt(), token.id, null)
                        attendeeViewModel.completeOrder(charge)
                    }

                    override fun onError(error: Exception) {
                        Snackbar.make(
                            rootView, error.localizedMessage.toString(), Snackbar.LENGTH_LONG
                        ).show()
                    }
                })
    }

    private fun loadEventDetails(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)
        val currency = Currency.getInstance(event.paymentCurrency)
        paymentCurrency = currency.symbol
        ticketsRecyclerAdapter.setCurrency(paymentCurrency)

        rootView.eventName.text = "${event.name} - ${EventUtils.getFormattedDate(startsAt)}"
        rootView.amount.text = "Total: $paymentCurrency$amount"
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
            .append(" - ")
            .append(EventUtils.getFormattedDate(endsAt))
            .append(" • ")
            .append(EventUtils.getFormattedTime(startsAt))
    }

    private fun openOrderCompletedFragment() {
        attendeeViewModel.paymentCompleted.value = false
        OrderCompletedFragmentArgs.Builder()
            .setEventId(safeArgs.eventId)
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.orderCompletedFragment, bundle, getAnimFade())
            }
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
                identifierList.add(form.fieldIdentifier)
                editTextList.add(editTextSection)
            }
        }
    }

    private fun getAttendeeField(identifier: String): String {
        val index = identifierList.indexOf(identifier)
        return if (index == -1) "" else index.let { editTextList[it] }.text.toString()
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
