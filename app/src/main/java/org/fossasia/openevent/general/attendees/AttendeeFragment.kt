package org.fossasia.openevent.general.attendees

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.fragment_attendee.*
import kotlinx.android.synthetic.main.fragment_attendee.view.*
import org.fossasia.openevent.general.AuthActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.order.OrderCompletedFragment
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.fossasia.openevent.general.ticket.TicketDetailsRecyclerAdapter
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import java.util.*

private const val STRIPE_KEY = "com.stripe.android.API_KEY"
private const val PRIVACY_POLICY = "https://eventyay.com/privacy-policy/"
private const val TERMS_OF_SERVICE = "https://eventyay.com/terms/"

class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private var id: Long = -1
    private val attendeeFragmentViewModel by viewModel<AttendeeViewModel>()
    private val ticketsRecyclerAdapter: TicketDetailsRecyclerAdapter = TicketDetailsRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var eventId: EventId
    private var ticketIdAndQty: List<Pair<Int, Int>>? = null
    private lateinit var selectedPaymentOption: String
    private lateinit var paymentCurrency: String
    private var expiryMonth: Int = -1
    private lateinit var expiryYear: String
    private lateinit var cardBrand: String

    private lateinit var API_KEY: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
            eventId = EventId(id)
            ticketIdAndQty = bundle.getSerializable(TICKET_ID_AND_QTY) as List<Pair<Int, Int>>
        }
        API_KEY = activity?.packageManager?.getApplicationInfo(activity?.packageName, PackageManager.GET_META_DATA)
                ?.metaData?.getString(STRIPE_KEY).toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Attendee Details"
        setHasOptionsMenu(true)

        val paragraph = SpannableStringBuilder()
        val startText = "I accept the "
        val termsText = "terms of service "
        val middleText = "and have read the "
        val privacyText = "privacy policy."

        paragraph.append(startText)
        paragraph.append(termsText)
        paragraph.append(middleText)
        paragraph.append(privacyText)

        val termsSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                context?.let {
                    Utils.openUrl(it, TERMS_OF_SERVICE)
                }
            }
        }

        val privacyPolicySpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                context?.let {
                    Utils.openUrl(it, PRIVACY_POLICY)
                }
            }
        }

        paragraph.setSpan(termsSpan, startText.length, startText.length + termsText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        paragraph.setSpan(privacyPolicySpan, paragraph.length - privacyText.length, paragraph.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // -1 so that we don't include "." in the link

        rootView.accept.text = paragraph
        rootView.accept.movementMethod = LinkMovementMethod.getInstance()

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        attendeeFragmentViewModel.ticketDetails(ticketIdAndQty)

        attendeeFragmentViewModel.updatePaymentSelectorVisibility(ticketIdAndQty)
        val paymentOptions = ArrayList<String>()
        paymentOptions.add("PayPal")
        paymentOptions.add("Stripe")
        attendeeFragmentViewModel.paymentSelectorVisibility.observe(this, Observer {
            if (it != null && it) {
                rootView.paymentSelector.visibility = View.VISIBLE
            } else {
                rootView.paymentSelector.visibility = View.GONE
            }

        })
        rootView.paymentSelector.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, paymentOptions)
        rootView.paymentSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedPaymentOption = paymentOptions[p2]
                if (selectedPaymentOption == "Stripe")
                    rootView.stripePayment.visibility = View.VISIBLE
                else
                    rootView.stripePayment.visibility = View.GONE
            }
        }

        attendeeFragmentViewModel.initializeSpinner()

        rootView.month.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, attendeeFragmentViewModel.month)
        rootView.month.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                expiryMonth = p2
                rootView.monthText.text = attendeeFragmentViewModel.month[p2]
            }
        }

        rootView.year.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, attendeeFragmentViewModel.year)
        rootView.year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                expiryYear = attendeeFragmentViewModel.year[p2]
                if (expiryYear == "Year")
                    expiryYear = "2017" //invalid year, if the user hasn't selected the year
                rootView.yearText.text = attendeeFragmentViewModel.year[p2]
            }
        }


        rootView.cardSelector.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, attendeeFragmentViewModel.cardType)
        rootView.cardSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cardBrand = attendeeFragmentViewModel.cardType[p2]
                rootView.selectCard.text = cardBrand
            }
        }
        attendeeFragmentViewModel.qtyList.observe(this, Observer {
            it?.let { it1 -> ticketsRecyclerAdapter.setQty(it1) }
        })

        rootView.view.setOnClickListener {
            if (rootView.view.text == "(view)") {
                rootView.ticketDetails.visibility = View.VISIBLE
                rootView.view.text = "(hide)"
            } else {
                rootView.ticketDetails.visibility = View.GONE
                rootView.view.text = "(view)"
            }
        }

        attendeeFragmentViewModel.loadEvent(id)

        if (attendeeFragmentViewModel.isLoggedIn()) {

            attendeeFragmentViewModel.loadUser(attendeeFragmentViewModel.getId())
            attendeeFragmentViewModel.loadEvent(id)

            attendeeFragmentViewModel.message.observe(this, Observer {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            })

            attendeeFragmentViewModel.progress.observe(this, Observer {
                it?.let { Utils.showProgressBar(rootView.progressBarAttendee, it) }
            })

            attendeeFragmentViewModel.event.observe(this, Observer {
                it?.let { loadEventDetails(it) }
                attendeeFragmentViewModel.totalAmount.observe(this, Observer {
                    rootView.amount.text = "Total: $paymentCurrency$it"
                })
            })

            attendeeFragmentViewModel.tickets.observe(this, Observer {
                it?.let {
                    ticketsRecyclerAdapter.addAll(it)
                }
                ticketsRecyclerAdapter.notifyDataSetChanged()
            })

            attendeeFragmentViewModel.totalQty.observe(this, Observer {
                rootView.qty.text = " — $it items"
            })

            attendeeFragmentViewModel.countryVisibility.observe(this, Observer {
                if (it != null) {
                    rootView.countryArea.visibility = if (it) View.VISIBLE else View.GONE
                }
            })

            attendeeFragmentViewModel.paymentCompleted.observe(this, Observer {
                if (it != null && it)
                    openOrderCompletedFragment()
            })

            attendeeFragmentViewModel.attendee.observe(this, Observer {
                it?.let {
                    helloUser.text = "Hello ${it.firstName.nullToEmpty()}"
                    firstName.text = Editable.Factory.getInstance().newEditable(it.firstName.nullToEmpty())
                    lastName.text = Editable.Factory.getInstance().newEditable(it.lastName.nullToEmpty())
                    email.text = Editable.Factory.getInstance().newEditable(it.email.nullToEmpty())
                }
            })

            rootView.signOut.setOnClickListener {
                attendeeFragmentViewModel.logout()
                redirectToLogin()
            }

            rootView.register.setOnClickListener {
                if (selectedPaymentOption == "Stripe")
                    sendToken()

                val attendees = ArrayList<Attendee>()
                ticketIdAndQty?.forEach {
                    if (it.second > 0) {
                        val attendee = Attendee(id = attendeeFragmentViewModel.getId(),
                                firstname = firstName.text.toString(),
                                lastname = lastName.text.toString(),
                                email = email.text.toString(),
                                ticket = TicketId(it.first.toLong()),
                                event = eventId)
                        attendees.add(attendee)
                    }
                }
                val country = if (country.text.isEmpty()) country.text.toString() else null
                attendeeFragmentViewModel.createAttendees(attendees, country, selectedPaymentOption)
            }
        } else {
            redirectToLogin()
            Toast.makeText(context, "You need to log in first!", Toast.LENGTH_LONG).show()
        }

        attendeeFragmentViewModel.ticketSoldOut.observe(this, Observer {
            it?.let {
                showTicketSoldOutDialog(it)
            }
        })
        return rootView
    }

    private fun showTicketSoldOutDialog(show: Boolean) {
        if (show) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(context?.resources?.getString(R.string.tickets_sold_out))
                    .setPositiveButton(context?.resources?.getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(activity, AuthActivity::class.java))
    }

    private fun sendToken() {
        val cardDetails: Card? = Card(cardNumber.text.toString(), expiryMonth, expiryYear.toInt(), cvc.text.toString())
        cardDetails?.addressCountry = country.text.toString()
        cardDetails?.addressZip = postalCode.text.toString()

        if (cardDetails?.brand != null && cardDetails.brand != "Unknown")
            rootView.selectCard.text = "Pay by ${cardDetails?.brand}"

        val validDetails: Boolean? = cardDetails?.validateCard()
        if (validDetails != null && !validDetails) {
            Toast.makeText(context, "Invalid card data", Toast.LENGTH_LONG).show()
        }

        cardDetails?.let {
            context?.let { contextIt ->
                Stripe(contextIt).createToken(
                        it,
                        API_KEY,
                        object : TokenCallback {
                            override fun onSuccess(token: Token) {
                                //Send this token to server
                                val charge = Charge(attendeeFragmentViewModel.getId().toInt(), token.id, null)
                                attendeeFragmentViewModel.completeOrder(charge)

                            }

                            override fun onError(error: Exception) {
                                Toast.makeText(context, error.localizedMessage.toString(), Toast.LENGTH_LONG).show()
                            }
                        })
            }
        }
    }

    private fun loadEventDetails(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        val currency = Currency.getInstance(event.paymentCurrency)
        paymentCurrency = currency.symbol
        ticketsRecyclerAdapter.setCurrency(paymentCurrency)

        rootView.eventName.text = "${event.name} - ${EventUtils.getFormattedDate(startsAt)}"
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
                .append(" - ")
                .append(EventUtils.getFormattedDate(endsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
    }

    private fun openOrderCompletedFragment() {
        attendeeFragmentViewModel.paymentCompleted.value = false
        //Initialise Order Completed Fragment
        val orderCompletedFragment = OrderCompletedFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", id)
        orderCompletedFragment.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.rootLayout, orderCompletedFragment)
                ?.addToBackStack(null)?.commit()
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroyView()
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
}