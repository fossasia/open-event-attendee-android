package org.fossasia.openevent.general.attendees

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.android.sdk.payments.PaymentConfirmation
import com.paypal.android.sdk.payments.ShippingAddress
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import java.math.BigDecimal
import java.util.Calendar
import java.util.Currency
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.fragment_attendee.view.accept
import kotlinx.android.synthetic.main.fragment_attendee.view.acceptCheckbox
import kotlinx.android.synthetic.main.fragment_attendee.view.amount
import kotlinx.android.synthetic.main.fragment_attendee.view.attendeeRecycler
import kotlinx.android.synthetic.main.fragment_attendee.view.attendeeScrollView
import kotlinx.android.synthetic.main.fragment_attendee.view.bankRadioButton
import kotlinx.android.synthetic.main.fragment_attendee.view.billingAddress
import kotlinx.android.synthetic.main.fragment_attendee.view.billingAddressLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.billingCity
import kotlinx.android.synthetic.main.fragment_attendee.view.billingCityLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.billingCompany
import kotlinx.android.synthetic.main.fragment_attendee.view.billingCompanyLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.billingEnabledCheckbox
import kotlinx.android.synthetic.main.fragment_attendee.view.billingInfoCheckboxSection
import kotlinx.android.synthetic.main.fragment_attendee.view.billingInfoContainer
import kotlinx.android.synthetic.main.fragment_attendee.view.billingPostalCode
import kotlinx.android.synthetic.main.fragment_attendee.view.billingPostalCodeLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.billingState
import kotlinx.android.synthetic.main.fragment_attendee.view.cancelButton
import kotlinx.android.synthetic.main.fragment_attendee.view.cardNumber
import kotlinx.android.synthetic.main.fragment_attendee.view.cardNumberLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.chequeRadioButton
import kotlinx.android.synthetic.main.fragment_attendee.view.countryPicker
import kotlinx.android.synthetic.main.fragment_attendee.view.cvc
import kotlinx.android.synthetic.main.fragment_attendee.view.cvcLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.email
import kotlinx.android.synthetic.main.fragment_attendee.view.emailLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.eventName
import kotlinx.android.synthetic.main.fragment_attendee.view.firstName
import kotlinx.android.synthetic.main.fragment_attendee.view.firstNameLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.helloUser
import kotlinx.android.synthetic.main.fragment_attendee.view.lastName
import kotlinx.android.synthetic.main.fragment_attendee.view.lastNameLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.loginButton
import kotlinx.android.synthetic.main.fragment_attendee.view.month
import kotlinx.android.synthetic.main.fragment_attendee.view.monthText
import kotlinx.android.synthetic.main.fragment_attendee.view.offlinePayment
import kotlinx.android.synthetic.main.fragment_attendee.view.offlinePaymentDescription
import kotlinx.android.synthetic.main.fragment_attendee.view.onSiteRadioButton
import kotlinx.android.synthetic.main.fragment_attendee.view.paymentOptionsGroup
import kotlinx.android.synthetic.main.fragment_attendee.view.paymentSelectorContainer
import kotlinx.android.synthetic.main.fragment_attendee.view.paypalRadioButton
import kotlinx.android.synthetic.main.fragment_attendee.view.qty
import kotlinx.android.synthetic.main.fragment_attendee.view.register
import kotlinx.android.synthetic.main.fragment_attendee.view.sameBuyerCheckBox
import kotlinx.android.synthetic.main.fragment_attendee.view.signInEditLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.signInEmail
import kotlinx.android.synthetic.main.fragment_attendee.view.signInEmailLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.signInLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.signInPassword
import kotlinx.android.synthetic.main.fragment_attendee.view.signInPasswordLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.signInText
import kotlinx.android.synthetic.main.fragment_attendee.view.signInTextLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.signOut
import kotlinx.android.synthetic.main.fragment_attendee.view.signOutLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.stripePayment
import kotlinx.android.synthetic.main.fragment_attendee.view.stripeRadioButton
import kotlinx.android.synthetic.main.fragment_attendee.view.taxId
import kotlinx.android.synthetic.main.fragment_attendee.view.taxLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.taxPrice
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketDetails
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketTableDetails
import kotlinx.android.synthetic.main.fragment_attendee.view.ticketsRecycler
import kotlinx.android.synthetic.main.fragment_attendee.view.time
import kotlinx.android.synthetic.main.fragment_attendee.view.timeoutCounterLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.timeoutInfoTextView
import kotlinx.android.synthetic.main.fragment_attendee.view.timeoutTextView
import kotlinx.android.synthetic.main.fragment_attendee.view.totalAmountLayout
import kotlinx.android.synthetic.main.fragment_attendee.view.totalPrice
import kotlinx.android.synthetic.main.fragment_attendee.view.year
import kotlinx.android.synthetic.main.fragment_attendee.view.yearText
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.ticket.TicketDetailsRecyclerAdapter
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.StringUtils.getTermsAndPolicyText
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.checkEmpty
import org.fossasia.openevent.general.utils.checkValidEmail
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.setRequired
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val PAYPAL_REQUEST_CODE = 101

class AttendeeFragment : Fragment(), ComplexBackPressFragment {

    private lateinit var rootView: View
    private val attendeeViewModel by viewModel<AttendeeViewModel>()
    private val ticketsRecyclerAdapter: TicketDetailsRecyclerAdapter = TicketDetailsRecyclerAdapter()
    private val attendeeRecyclerAdapter: AttendeeRecyclerAdapter = AttendeeRecyclerAdapter()
    private val safeArgs: AttendeeFragmentArgs by navArgs()
    private lateinit var timer: CountDownTimer
    private lateinit var card: Card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (attendeeViewModel.ticketIdAndQty == null) {
            attendeeViewModel.ticketIdAndQty = safeArgs.ticketIdAndQty?.value
            attendeeViewModel.singleTicket = safeArgs.ticketIdAndQty?.value?.map { it.second }?.sum() == 1
        }

        attendeeRecyclerAdapter.setEventId(safeArgs.eventId)
        if (attendeeViewModel.paymentCurrency.isNotBlank())
            ticketsRecyclerAdapter.setCurrency(attendeeViewModel.paymentCurrency)
        safeArgs.ticketIdAndQty?.value?.let {
            val quantities = it.map { pair -> pair.second }.filter { it != 0 }
            val donations = it.filter { it.second != 0 }.map { pair -> pair.third * pair.second }
            ticketsRecyclerAdapter.setQuantity(quantities)
            ticketsRecyclerAdapter.setDonations(donations)
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

        val progressDialog = Utils.progressDialog(context, getString(R.string.creating_order_message))
        attendeeViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        attendeeViewModel.redirectToProfile
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(getString(R.string.verify_your_profile))
                findNavController(rootView).navigate(
                    AttendeeFragmentDirections.actionTicketsToProfile()
                )
            })

        rootView.sameBuyerCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val firstName = rootView.firstName.text.toString()
                val lastName = rootView.lastName.text.toString()
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    rootView.longSnackbar(getString(R.string.fill_required_fields_message))
                    rootView.sameBuyerCheckBox.isChecked = false
                    return@setOnCheckedChangeListener
                }
                attendeeRecyclerAdapter.setFirstAttendee(
                    Attendee(firstname = firstName,
                        lastname = lastName,
                        email = rootView.email.text.toString(),
                        id = attendeeViewModel.getId())
                )
            } else {
                attendeeRecyclerAdapter.setFirstAttendee(null)
            }
        }

        setupEventInfo()
        setupPendingOrder()
        setupTicketDetailTable()
        setupSignOutLogIn()
        setupAttendeeDetails()
        setupCustomForms()
        setupBillingInfo()
        setupCountryOptions()
        setupCardNumber()
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
            rootView.attendeeScrollView.longSnackbar(getString(R.string.no_internet_connection_message))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        attendeeRecyclerAdapter.attendeeChangeListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::timer.isInitialized)
            timer.cancel()
        activity?.stopService(Intent(activity, PayPalService::class.java))
    }

    override fun handleBackPress() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cancel_order))
            .setMessage(getString(R.string.cancel_order_message))
            .setPositiveButton(getString(R.string.cancel_order_button)) { _, _ ->
                findNavController(rootView).popBackStack()
            }.setNeutralButton(getString(R.string.continue_order_button)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    private fun setupEventInfo() {
        attendeeViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEventDetailsUI(it)
                setupPaymentOptions(it)
            })

        attendeeViewModel.getSettings()
        attendeeViewModel.orderExpiryTime
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                setupCountDownTimer(it)
            })

        val currentEvent = attendeeViewModel.event.value
        if (currentEvent == null)
            attendeeViewModel.loadEvent(safeArgs.eventId)
        else {
            setupPaymentOptions(currentEvent)
            loadEventDetailsUI(currentEvent)
        }
        rootView.register.text = if (safeArgs.amount > 0) getString(R.string.pay_now) else getString(R.string.register)
    }

    private fun setupPendingOrder() {
        val currentPendingOrder = attendeeViewModel.pendingOrder.value
        if (currentPendingOrder == null) {
            attendeeViewModel.initializeOrder(safeArgs.eventId)
        }
    }

    private fun setupCountDownTimer(orderExpiryTime: Int) {
        rootView.timeoutCounterLayout.isVisible = true
        rootView.timeoutInfoTextView.text =
            getString(R.string.ticket_timeout_info_message, orderExpiryTime.toString())

        val timeLeft: Long = if (attendeeViewModel.timeout == -1L) orderExpiryTime * 60 * 1000L
                                else attendeeViewModel.timeout
        timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onFinish() {
                findNavController(rootView).navigate(AttendeeFragmentDirections
                    .actionAttendeeToTicketPop(safeArgs.eventId, safeArgs.currency, true))
            }

            override fun onTick(millisUntilFinished: Long) {
                attendeeViewModel.timeout = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                rootView.timeoutTextView.text = "$minutes:$seconds"
            }
        }
        timer.start()
    }

    private fun setupTicketDetailTable() {
        attendeeViewModel.ticketIdAndQty?.map { it.second }?.sum()?.let {
            rootView.qty.text = resources.getQuantityString(R.plurals.order_quantity_item, it, it)
        }
        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        rootView.taxLayout.isVisible = safeArgs.taxAmount > 0f
        rootView.taxPrice.text = "${Currency.getInstance(safeArgs.currency).symbol}${"%.2f".format(safeArgs.taxAmount)}"
        rootView.totalAmountLayout.isVisible = safeArgs.amount > 0f
        rootView.totalPrice.text = "${Currency.getInstance(safeArgs.currency).symbol}${"%.2f".format(safeArgs.amount)}"

        rootView.ticketTableDetails.setOnClickListener {
            attendeeViewModel.ticketDetailsVisible = !attendeeViewModel.ticketDetailsVisible
            loadTicketDetailsTableUI(attendeeViewModel.ticketDetailsVisible)
        }
        loadTicketDetailsTableUI(attendeeViewModel.ticketDetailsVisible)

        attendeeViewModel.totalAmount.value = safeArgs.amount
        rootView.paymentSelectorContainer.isVisible = safeArgs.amount > 0

        attendeeViewModel.tickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer { tickets ->
                ticketsRecyclerAdapter.addAll(tickets)
                attendeeRecyclerAdapter.addAllTickets(tickets)
            })

        val currentTickets = attendeeViewModel.tickets.value
        if (currentTickets != null) {
            rootView.paymentSelectorContainer.isVisible = safeArgs.amount > 0

            ticketsRecyclerAdapter.addAll(currentTickets)
            attendeeRecyclerAdapter.addAllTickets(currentTickets)
        } else {
            attendeeViewModel.getTickets()
        }
    }

    private fun setupSignOutLogIn() {
        rootView.signInEmailLayout.setRequired()
        rootView.signInPasswordLayout.setRequired()
        rootView.firstNameLayout.setRequired()
        rootView.lastNameLayout.setRequired()
        rootView.emailLayout.setRequired()

        setupSignInLayout()
        setupUser()
        setupLoginSignoutClickListener()

        attendeeViewModel.signedIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer { signedIn ->
                rootView.signInLayout.isVisible = !signedIn
                rootView.signOutLayout.isVisible = signedIn
                if (signedIn) {
                    rootView.sameBuyerCheckBox.isVisible = true
                    attendeeViewModel.loadUser()
                } else {
                    attendeeViewModel.isShowingSignInText = true
                    handleSignedOut()
                }
            })

        if (attendeeViewModel.isLoggedIn()) {
            rootView.signInLayout.isVisible = false
            rootView.signOutLayout.isVisible = true
            rootView.sameBuyerCheckBox.isVisible = true
            val currentUser = attendeeViewModel.user.value
            if (currentUser == null)
                attendeeViewModel.loadUser()
            else
                loadUserUI(currentUser)
        } else {
            handleSignedOut()
        }
    }

    private fun handleSignedOut() {
        rootView.signInEditLayout.isVisible = !attendeeViewModel.isShowingSignInText
        rootView.signInTextLayout.isVisible = attendeeViewModel.isShowingSignInText
        rootView.email.setText("")
        rootView.firstName.setText("")
        rootView.lastName.setText("")
        rootView.sameBuyerCheckBox.isChecked = false
        rootView.sameBuyerCheckBox.isVisible = false
    }

    private fun setupSignInLayout() {
        val stringBuilder = SpannableStringBuilder()
        val signIn = getString(R.string.sign_in)
        val signInForOrderText = getString(R.string.sign_in_order_text)
        stringBuilder.append(signIn)
        stringBuilder.append(" $signInForOrderText")
        val signInSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                rootView.signInTextLayout.isVisible = false
                rootView.signInEditLayout.isVisible = true
                attendeeViewModel.isShowingSignInText = false
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        stringBuilder.setSpan(signInSpan, 0, signIn.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        rootView.signInText.text = stringBuilder
        rootView.signInText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupLoginSignoutClickListener() {
        rootView.cancelButton.setOnClickListener {
            rootView.signInTextLayout.isVisible = true
            rootView.signInEditLayout.isVisible = false
            attendeeViewModel.isShowingSignInText = true
        }

        rootView.loginButton.setOnClickListener {
            var validForLogin = true
            validForLogin = rootView.signInEmail.checkEmpty(rootView.signInEmailLayout) && validForLogin
            validForLogin = rootView.signInEmail.checkValidEmail(rootView.signInEmailLayout) && validForLogin
            validForLogin = rootView.signInPassword.checkEmpty(rootView.signInEmailLayout) && validForLogin
            if (validForLogin)
                attendeeViewModel.login(rootView.signInEmail.text.toString(), rootView.signInPassword.text.toString())
        }

        rootView.signOut.setOnClickListener {
            if (!isNetworkConnected(context)) {
                rootView.snackbar(getString(R.string.no_internet_connection_message))
                return@setOnClickListener
            }
            attendeeViewModel.logOut()
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
                        email = rootView.email.text.toString(),
                        ticket = TicketId(ticket),
                        event = EventId(safeArgs.eventId))
                    attendeeViewModel.attendees.clear()
                    attendeeViewModel.attendees.add(attendee)
                }
            })
    }

    private fun setupAttendeeDetails() {
        rootView.attendeeRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.attendeeRecycler.adapter = attendeeRecyclerAdapter
        rootView.attendeeRecycler.isNestedScrollingEnabled = false

        if (attendeeViewModel.attendees.isEmpty()) {
            attendeeViewModel.ticketIdAndQty?.let {
                it.forEach { pair ->
                    repeat(pair.second) {
                        attendeeViewModel.attendees.add(Attendee(
                            id = attendeeViewModel.getId(), firstname = "", lastname = "", email = "",
                            ticket = TicketId(pair.first.toLong()), event = EventId(safeArgs.eventId)
                        ))
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
                attendeeRecyclerAdapter.setCustomForm(it)
            })

        val currentForms = attendeeViewModel.forms.value
        if (currentForms == null) {
            attendeeViewModel.getCustomFormsForAttendees(safeArgs.eventId)
        } else {
            attendeeRecyclerAdapter.setCustomForm(currentForms)
        }
    }

    private fun setupBillingInfo() {
        rootView.billingInfoCheckboxSection.isVisible = safeArgs.amount > 0
        rootView.billingCompanyLayout.setRequired()
        rootView.billingAddressLayout.setRequired()
        rootView.billingCityLayout.setRequired()
        rootView.billingPostalCodeLayout.setRequired()
        rootView.billingInfoContainer.isVisible = rootView.billingEnabledCheckbox.isChecked
        attendeeViewModel.billingEnabled = rootView.billingEnabledCheckbox.isChecked
        rootView.billingEnabledCheckbox.setOnCheckedChangeListener { _, isChecked ->
            attendeeViewModel.billingEnabled = isChecked
            rootView.billingInfoContainer.isVisible = isChecked
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
    }

    private fun setupPaymentOptions(event: Event) {
        rootView.paypalRadioButton.isVisible = event.canPayByPaypal
        rootView.stripeRadioButton.isVisible = event.canPayByStripe
        rootView.chequeRadioButton.isVisible = event.canPayByCheque
        rootView.bankRadioButton.isVisible = event.canPayByBank
        rootView.onSiteRadioButton.isVisible = event.canPayOnsite

        rootView.paymentOptionsGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.stripeRadioButton -> {
                    rootView.stripePayment.isVisible = true
                    rootView.offlinePayment.isVisible = false
                    attendeeViewModel.selectedPaymentMode = PAYMENT_MODE_STRIPE
                    rootView.register.text = getString(R.string.pay_now)
                }
                R.id.onSiteRadioButton -> {
                    rootView.offlinePayment.isVisible = true
                    rootView.stripePayment.isVisible = false
                    rootView.offlinePaymentDescription.text = event.onsiteDetails
                    attendeeViewModel.selectedPaymentMode = PAYMENT_MODE_ONSITE
                    rootView.register.text = getString(R.string.register)
                }
                R.id.bankRadioButton -> {
                    rootView.offlinePayment.isVisible = true
                    rootView.stripePayment.isVisible = false
                    rootView.offlinePaymentDescription.text = event.bankDetails
                    attendeeViewModel.selectedPaymentMode = PAYMENT_MODE_BANK
                    rootView.register.text = getString(R.string.register)
                }
                R.id.chequeRadioButton -> {
                    rootView.offlinePayment.isVisible = true
                    rootView.stripePayment.isVisible = false
                    rootView.offlinePaymentDescription.text = event.chequeDetails
                    attendeeViewModel.selectedPaymentMode = PAYMENT_MODE_CHEQUE
                    rootView.register.text = getString(R.string.register)
                }
                else -> {
                    rootView.stripePayment.isVisible = false
                    rootView.offlinePayment.isVisible = false
                    attendeeViewModel.selectedPaymentMode = PAYMENT_MODE_PAYPAL
                    rootView.register.text = getString(R.string.pay_now)
                }
            }
        }
    }

    private fun setupCardNumber() {
        rootView.cardNumberLayout.setRequired()
        rootView.cvcLayout.setRequired()
        rootView.cardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /*Do Nothing*/ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    val cardType = Utils.getCardType(s.toString())
                    if (cardType == Utils.cardType.NONE) {
                        rootView.cardNumberLayout.error = getString(R.string.invalid_card_number_message)
                        return
                    }
                }
                rootView.cardNumber.error = null
            }
        })
        attendeeViewModel.stripeOrderMade
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it && this::card.isInitialized)
                    sendToken(card)
            })

        attendeeViewModel.paypalOrderMade
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                startPaypalPayment()
            })
    }

    private fun setupMonthOptions() {
        val month = mutableListOf(
            getString(R.string.month_string),
            getString(R.string.january),
            getString(R.string.february),
            getString(R.string.march),
            getString(R.string.april),
            getString(R.string.may),
            getString(R.string.june),
            getString(R.string.july),
            getString(R.string.august),
            getString(R.string.september),
            getString(R.string.october),
            getString(R.string.november),
            getString(R.string.december)
        )

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

    private fun setupTermsAndCondition() {
        rootView.accept.text = getTermsAndPolicyText(requireContext(), resources)
        rootView.accept.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun checkPaymentOptions(): Boolean =
        when (attendeeViewModel.selectedPaymentMode) {
            PAYMENT_MODE_STRIPE -> {
                card = Card.create(rootView.cardNumber.text.toString(), attendeeViewModel.monthSelectedPosition,
                    rootView.year.selectedItem.toString().toInt(), rootView.cvc.text.toString())

                if (!card.validateCard()) {
                    rootView.snackbar(getString(R.string.invalid_card_data_message))
                    false
                } else {
                    true
                }
            }
            PAYMENT_MODE_CHEQUE, PAYMENT_MODE_ONSITE, PAYMENT_MODE_FREE, PAYMENT_MODE_BANK, PAYMENT_MODE_PAYPAL -> true
            else -> {
                rootView.snackbar(getString(R.string.select_payment_option_message))
                false
            }
        }

    private fun startPaypalPayment() {
        val paypalEnvironment = if (BuildConfig.DEBUG) PayPalConfiguration.ENVIRONMENT_SANDBOX
            else PayPalConfiguration.ENVIRONMENT_PRODUCTION
        val paypalConfig = PayPalConfiguration()
            .environment(paypalEnvironment)
            .clientId(BuildConfig.PAYPAL_CLIENT_ID)
        val paypalIntent = Intent(activity, PaymentActivity::class.java)
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        activity?.startService(paypalIntent)

        val paypalPayment = paypalThingsToBuy(PayPalPayment.PAYMENT_INTENT_SALE)
        val payeeEmail = attendeeViewModel.event.value?.paypalEmail ?: ""
        paypalPayment.payeeEmail(payeeEmail)
        addShippingAddress(paypalPayment)
        val intent = Intent(activity, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
        startActivityForResult(intent, PAYPAL_REQUEST_CODE)
    }

    private fun addShippingAddress(paypalPayment: PayPalPayment) {
        if (rootView.billingEnabledCheckbox.isChecked) {
            val shippingAddress = ShippingAddress()
                .recipientName("${rootView.firstName.text} ${rootView.lastName.text}")
                .line1(rootView.billingAddress.text.toString())
                .city(rootView.billingCity.text.toString())
                .state(rootView.billingState.text.toString())
                .postalCode(rootView.billingPostalCode.text.toString())
                .countryCode(getCountryCodes(rootView.countryPicker.selectedItem.toString()))
            paypalPayment.providedShippingAddress(shippingAddress)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYPAL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val paymentConfirm =
                data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
            if (paymentConfirm != null) {
                val paymentId = paymentConfirm.proofOfPayment.paymentId
                attendeeViewModel.sendPaypalConfirm(paymentId)
            }
        }
    }

    private fun paypalThingsToBuy(paymentIntent: String): PayPalPayment =
        PayPalPayment(BigDecimal(safeArgs.amount.toString()),
            Currency.getInstance(safeArgs.currency).currencyCode,
            getString(R.string.tickets_for, attendeeViewModel.event.value?.name), paymentIntent)

    private fun checkRequiredFields(): Boolean {
        val checkBasicInfo = rootView.firstName.checkEmpty(rootView.firstNameLayout) &&
            rootView.lastName.checkEmpty(rootView.lastNameLayout) &&
            rootView.email.checkEmpty(rootView.emailLayout)

        var checkBillingInfo = true
        if (rootView.billingEnabledCheckbox.isChecked) {
            checkBillingInfo = rootView.billingCompany.checkEmpty(rootView.billingCompanyLayout) && checkBillingInfo
            checkBillingInfo = rootView.billingAddress.checkEmpty(rootView.billingAddressLayout) && checkBillingInfo
            checkBillingInfo = rootView.billingCity.checkEmpty(rootView.billingCityLayout) && checkBillingInfo
            checkBillingInfo = rootView.billingPostalCode.checkEmpty(rootView.billingPostalCodeLayout) &&
                checkBillingInfo
        }

        var checkStripeInfo = true
        if (safeArgs.amount != 0F && attendeeViewModel.selectedPaymentMode == PAYMENT_MODE_STRIPE) {
            checkStripeInfo = rootView.cardNumber.checkEmpty(rootView.cardNumberLayout) &&
                rootView.cvc.checkEmpty(rootView.cvcLayout)
        }

        return checkAttendeesInfo() && checkBasicInfo && checkBillingInfo && checkStripeInfo
    }

    private fun checkAttendeesInfo(): Boolean {
        var valid = true
        for (pos in 0..attendeeRecyclerAdapter.itemCount) {
            val viewHolderItem = rootView.attendeeRecycler.findViewHolderForAdapterPosition(pos)
            if (viewHolderItem is AttendeeViewHolder) {
                if (!viewHolderItem.checkValidFields()) valid = false
            }
        }
        return valid
    }

    private fun setupRegisterOrder() {
        rootView.register.setOnClickListener {
            val currentUser = attendeeViewModel.user.value
            if (currentUser == null) {
                rootView.longSnackbar(getString(R.string.sign_in_to_order_ticket))
                return@setOnClickListener
            }
            if (!isNetworkConnected(context)) {
                rootView.attendeeScrollView.longSnackbar(getString(R.string.no_internet_connection_message))
                return@setOnClickListener
            }
            if (!rootView.acceptCheckbox.isChecked) {
                rootView.attendeeScrollView.longSnackbar(getString(R.string.term_and_conditions))
                return@setOnClickListener
            }

            if (!checkRequiredFields()) {
                rootView.snackbar(R.string.fill_required_fields_message)
                return@setOnClickListener
            }

            if (attendeeViewModel.totalAmount.value != 0F && !checkPaymentOptions()) return@setOnClickListener

            val attendees = attendeeViewModel.attendees

            if (attendeeViewModel.areAttendeeEmailsValid(attendees)) {
                val country = rootView.countryPicker.selectedItem.toString()
                val paymentOption =
                    if (safeArgs.amount != 0F) attendeeViewModel.selectedPaymentMode
                    else PAYMENT_MODE_FREE
                val company = rootView.billingCompany.text.toString()
                val city = rootView.billingCity.text.toString()
                val state = rootView.billingState.text.toString()
                val taxId = rootView.taxId.text.toString()
                val address = rootView.billingAddress.text.toString()
                val postalCode = rootView.billingPostalCode.text.toString()
                attendeeViewModel.createAttendees(attendees, country, company, taxId, address,
                    city, postalCode, state, paymentOption)
            } else {
                rootView.attendeeScrollView.longSnackbar(getString(R.string.invalid_email_address_message))
            }
        }

        attendeeViewModel.ticketSoldOut
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showTicketSoldOutDialog(it)
            })

        attendeeViewModel.orderCompleted
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

    private fun sendToken(card: Card) {
        Stripe(requireContext(), BuildConfig.STRIPE_API_KEY)
            .createCardToken(card = card, callback = object : ApiResultCallback<Token> {
                override fun onSuccess(token: Token) {
                    val charge = Charge(attendeeViewModel.getId().toInt(), token.id, null)
                    attendeeViewModel.chargeOrder(charge)
                }

                override fun onError(error: Exception) {
                    rootView.snackbar(error.localizedMessage.toString())
                }
            })
    }

    private fun loadEventDetailsUI(event: Event) {
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        attendeeViewModel.paymentCurrency = Currency.getInstance(event.paymentCurrency).symbol
        ticketsRecyclerAdapter.setCurrency(attendeeViewModel.paymentCurrency)

        rootView.eventName.text = event.name
        val total = if (safeArgs.amount > 0) "${attendeeViewModel.paymentCurrency} ${"%.2f".format(safeArgs.amount)}"
                else getString(R.string.free)
        rootView.amount.text = getString(R.string.total_amount, total)

        rootView.time.text = EventUtils.getFormattedDateTimeRangeDetailed(startsAt, endsAt)
    }

    private fun loadUserUI(user: User) {
        rootView.helloUser.text = getString(R.string.hello_user, user.firstName.nullToEmpty())
        rootView.firstName.text = SpannableStringBuilder(user.firstName.nullToEmpty())
        rootView.lastName.text = SpannableStringBuilder(user.lastName.nullToEmpty())
        rootView.email.text = SpannableStringBuilder(user.email.nullToEmpty())
        rootView.firstName.isEnabled = user.firstName.isNullOrEmpty()
        rootView.lastName.isEnabled = user.lastName.isNullOrEmpty()
        rootView.email.isEnabled = false
    }

    private fun loadTicketDetailsTableUI(show: Boolean) {
        if (show) {
            rootView.ticketDetails.isVisible = true
            rootView.ticketTableDetails.text = context?.getString(R.string.hide)
        } else {
            rootView.ticketDetails.isVisible = false
            rootView.ticketTableDetails.text = context?.getString(R.string.view)
        }
    }

    private fun openOrderCompletedFragment() {
        attendeeViewModel.orderCompleted.value = false
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

    private fun autoSetCurrentCountry() {
        val telephonyManager: TelephonyManager = activity?.getSystemService(Context.TELEPHONY_SERVICE)
            as TelephonyManager
        val currentCountryCode = telephonyManager.networkCountryIso
        val countryCodes = resources.getStringArray(R.array.country_code_arrays)
        val countryIndex = countryCodes.indexOf(currentCountryCode.toUpperCase())
        if (countryIndex != -1) rootView.countryPicker.setSelection(countryIndex)
    }

    private fun getCountryCodes(countryName: String): String {
        val countryCodes = resources.getStringArray(R.array.country_code_arrays)
        val countryList = resources.getStringArray(R.array.country_arrays)
        val index = countryList.indexOf(countryName)
        return countryCodes[index]
    }
}
