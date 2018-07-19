package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
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
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.fossasia.openevent.general.ticket.TicketDetailsRecyclerAdapter
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import java.util.*

private const val STRIPE_KEY = "com.stripe.android.API_KEY"

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
                    rootView.cardInputWidget.visibility = View.VISIBLE
                else
                    rootView.cardInputWidget.visibility = View.GONE
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
                    rootView.amount.text = "Total: $paymentCurrency $it"
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

            attendeeFragmentViewModel.attendee.observe(this, Observer {
                it?.let {
                    firstName.text = Editable.Factory.getInstance().newEditable(it.firstName.nullToEmpty())
                    lastName.text = Editable.Factory.getInstance().newEditable(it.lastName.nullToEmpty())
                    email.text = Editable.Factory.getInstance().newEditable(it.email.nullToEmpty())
                }
            })

            rootView.register.setOnClickListener {
                if (selectedPaymentOption == "Stripe")
                    sendToken()

                ticketIdAndQty?.forEach {
                    if (it.second > 0) {
                        val attendee = Attendee(id = attendeeFragmentViewModel.getId(),
                                firstname = firstName.text.toString(),
                                lastname = lastName.text.toString(),
                                email = email.text.toString(),
                                ticket = TicketId(it.first.toLong()),
                                event = eventId)
                        val country = country.text.toString()
                        attendeeFragmentViewModel.createAttendee(attendee, id, country, selectedPaymentOption)
                    }
                }
            }
        } else {
            redirectToLogin()
            Toast.makeText(context, "You need to log in first!", Toast.LENGTH_LONG).show()
        }

        return rootView
    }

    private fun redirectToLogin() {
        startActivity(Intent(activity, AuthActivity::class.java))
    }

    private fun sendToken() {
        val cardDetails: Card? = cardInputWidget.card

        if (cardDetails == null)
            Toast.makeText(context, "Invalid card data", Toast.LENGTH_LONG).show()

        cardDetails?.let {
            context?.let { contextIt ->
                Stripe(contextIt).createToken(
                        it,
                        API_KEY,
                        object : TokenCallback {
                            override fun onSuccess(token: Token) {
                                //Send this token to server
                                Toast.makeText(context, "Token received from Stripe", Toast.LENGTH_LONG).show()
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

        rootView.eventName.text = "${event.name} - ${EventUtils.getFormattedDate(startsAt)}"
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
                .append(" - ")
                .append(EventUtils.getFormattedDate(endsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
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