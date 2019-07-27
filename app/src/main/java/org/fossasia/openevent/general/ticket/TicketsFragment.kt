package org.fossasia.openevent.general.ticket

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_tickets.ticketsCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_tickets.view.eventName
import kotlinx.android.synthetic.main.fragment_tickets.view.organizerName
import kotlinx.android.synthetic.main.fragment_tickets.view.register
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketInfoTextView
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketTableHeader
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketsRecycler
import kotlinx.android.synthetic.main.fragment_tickets.view.time
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketsCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_tickets.view.discountCodeEditText
import kotlinx.android.synthetic.main.fragment_tickets.view.discountCodeLayout
import kotlinx.android.synthetic.main.fragment_tickets.view.discountCodeAppliedLayout
import kotlinx.android.synthetic.main.fragment_tickets.view.cancelDiscountCode
import kotlinx.android.synthetic.main.fragment_tickets.view.applyDiscountCode
import kotlinx.android.synthetic.main.fragment_tickets.view.applyButton
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.longSnackbar

const val TICKETS_FRAGMENT = "ticketsFragment"
const val APPLY_DISCOUNT_CODE = 1
private const val SHOW_DISCOUNT_CODE_LAYOUT = 2
private const val DISCOUNT_CODE_APPLIED = 3

class TicketsFragment : Fragment() {
    private val ticketsRecyclerAdapter: TicketsRecyclerAdapter = TicketsRecyclerAdapter()
    private val ticketsViewModel by viewModel<TicketsViewModel>()
    private val safeArgs: TicketsFragmentArgs by navArgs()
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var ticketIdAndQty = ArrayList<Triple<Int, Int, Float>>()
    private var totalAmount: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticketsRecyclerAdapter.setCurrency(safeArgs.currency)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tickets, container, false)
        setToolbar(activity, getString(R.string.ticket_details))
        setHasOptionsMenu(true)

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        val progressDialog = progressDialog(context, getString(R.string.loading_message))
        ticketsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
                rootView.ticketTableHeader.isGone = it
                rootView.register.isGone = it
            })

        ticketsViewModel.ticketTableVisibility
            .nonNull()
            .observe(viewLifecycleOwner, Observer { ticketTableVisible ->
                rootView.ticketTableHeader.isVisible = ticketTableVisible
                rootView.register.isVisible = ticketTableVisible
                rootView.ticketsRecycler.isVisible = ticketTableVisible
                rootView.ticketInfoTextView.isGone = ticketTableVisible
                rootView.applyDiscountCode.isVisible = ticketTableVisible
            })

        ticketsViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                handleDiscountCodeVisibility()
                rootView.discountCodeEditText.text = SpannableStringBuilder("")
                ticketsCoordinatorLayout.longSnackbar(it)
            })

        ticketsViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEventDetails(it)
            })

        ticketsViewModel.tickets
            .nonNull()
            .observe(this, Observer {
                ticketsRecyclerAdapter.addAll(it)
                ticketsRecyclerAdapter.notifyDataSetChanged()
            })

        rootView.register.setOnClickListener {
            if (!ticketsViewModel.totalTicketsEmpty(ticketIdAndQty)) {
                ticketsViewModel.getAmount(ticketIdAndQty)
            } else {
                showErrorMessage(resources.getString(R.string.no_tickets_message))
            }
        }

        ticketsViewModel.amount
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                totalAmount = it
                checkForAuthentication()
            })

        rootView.retry.setOnClickListener {
            loadTickets()
        }

        rootView.applyDiscountCode.setOnClickListener {
            handleDiscountCodeVisibility(SHOW_DISCOUNT_CODE_LAYOUT)
        }

        rootView.applyButton.setOnClickListener {
            if (rootView.discountCodeEditText.text.isNullOrEmpty()) {
                rootView.discountCodeEditText.error = getString(R.string.empty_field_error_message)
                return@setOnClickListener
            }
            hideSoftKeyboard(context, rootView)
            ticketsViewModel.fetchDiscountCode(rootView.discountCodeEditText.text.toString().trim())
        }

        ticketsViewModel.discountCode
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it.eventId?.id != safeArgs.eventId) {
                    handleDiscountCodeVisibility()
                    rootView.ticketsCoordinatorLayout.longSnackbar(getString(R.string.invalid_discount_code))
                    return@Observer
                }
                ticketsViewModel.appliedDiscountCode = it
                ticketsRecyclerAdapter.applyDiscount(it)
                ticketsRecyclerAdapter.notifyDataSetChanged()
                handleDiscountCodeVisibility(DISCOUNT_CODE_APPLIED)
            })

        rootView.cancelDiscountCode.setOnClickListener {
            rootView.discountCodeEditText.text = SpannableStringBuilder("")
            ticketsViewModel.appliedDiscountCode = null
            ticketsRecyclerAdapter.cancelDiscountCode()
            ticketsRecyclerAdapter.notifyDataSetChanged()
            handleDiscountCodeVisibility()
        }

        ticketsViewModel.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                loadTickets()
                showNoInternetScreen(!isConnected && ticketsViewModel.tickets.value == null)
            })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ticketSelectedListener = object : TicketSelectedListener {
            override fun onSelected(ticketId: Int, quantity: Int, donation: Float) {
                handleTicketSelect(ticketId, quantity, donation)
                ticketsViewModel.ticketIdAndQty.value = ticketIdAndQty
            }
        }
        ticketsRecyclerAdapter.setSelectListener(ticketSelectedListener)

        if (safeArgs.timeout) showErrorMessage(getString(R.string.ticket_timeout_message))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ticketsRecyclerAdapter.setSelectListener(null)
    }

    private fun checkForAuthentication() {
        if (ticketsViewModel.isLoggedIn())
            redirectToAttendee()
        else {
            ticketsCoordinatorLayout.longSnackbar(getString(R.string.log_in_first))
            redirectToLogin()
        }
    }

    private fun redirectToAttendee() {
        val wrappedTicketAndQty = TicketIdAndQtyWrapper(ticketIdAndQty)
        ticketsViewModel.mutableAmount.value = null
        findNavController(rootView).navigate(TicketsFragmentDirections.actionTicketsToAttendee(
            eventId = safeArgs.eventId,
            ticketIdAndQty = wrappedTicketAndQty,
            currency = safeArgs.currency,
            amount = totalAmount,
            hasPaidTickets = ticketsViewModel.hasPaidTickets
        ))
        ticketsViewModel.hasPaidTickets = false
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(TicketsFragmentDirections.actionTicketsToAuth(
            getString(R.string.log_in_first), TICKETS_FRAGMENT
        ))
    }

    private fun handleTicketSelect(id: Int, quantity: Int, donation: Float = 0F) {
        val pos = ticketIdAndQty.map { it.first }.indexOf(id)
        if (pos == -1) {
            ticketIdAndQty.add(Triple(id, quantity, donation))
        } else {
            ticketIdAndQty[pos] = Triple(id, quantity, donation)
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

    private fun loadEventDetails(event: Event) {
        rootView.eventName.text = event.name
        val organizerName = event.ownerName
        if (organizerName == null) {
            rootView.organizerName.isVisible = false
        } else {
            rootView.organizerName.isVisible = true
            rootView.organizerName.text = getString(R.string.by_organizer_name, organizerName)
        }
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)
        rootView.time.text = EventUtils.getFormattedDateTimeRangeDetailed(startsAt, endsAt)
        ticketsRecyclerAdapter.setTimeZone(event.timezone)
    }

    private fun showErrorMessage(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setTitle(resources.getString(R.string.whoops))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun loadTickets() {
        val currentEvent = ticketsViewModel.event.value
        if (currentEvent == null) {
            ticketsViewModel.loadEvent(safeArgs.eventId)
        } else {
            loadEventDetails(currentEvent)
        }
        if (ticketsViewModel.isConnected() && ticketsViewModel.tickets.value == null) {
            ticketsViewModel.loadTickets(safeArgs.eventId)
        }

        val retainedTicketIdAndQty: List<Triple<Int, Int, Float>>? = ticketsViewModel.ticketIdAndQty.value
        if (retainedTicketIdAndQty != null) {
            for (idAndQty in retainedTicketIdAndQty) {
                handleTicketSelect(idAndQty.first, idAndQty.second, idAndQty.third)
            }
            ticketsRecyclerAdapter.setTicketAndQty(retainedTicketIdAndQty)
            ticketsRecyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.noInternetCard.isVisible = show
        rootView.ticketTableHeader.isVisible = !show
        rootView.ticketsRecycler.isVisible = !show
        rootView.register.isVisible = !show
        if (show) {
            rootView.discountCodeLayout.isVisible = false
            rootView.discountCodeAppliedLayout.isVisible = false
            rootView.applyDiscountCode.isVisible = false
        } else {
            handleDiscountCodeVisibility(ticketsViewModel.discountCodeCurrentLayout)
        }
    }

    private fun handleDiscountCodeVisibility(code: Int = APPLY_DISCOUNT_CODE) {
        ticketsViewModel.discountCodeCurrentLayout = code
        when (code) {
            APPLY_DISCOUNT_CODE -> {
                rootView.applyDiscountCode.isVisible = true
                rootView.discountCodeAppliedLayout.isVisible = false
                rootView.discountCodeLayout.isVisible = false
            }
            SHOW_DISCOUNT_CODE_LAYOUT -> {
                rootView.applyDiscountCode.isVisible = false
                rootView.discountCodeAppliedLayout.isVisible = false
                rootView.discountCodeLayout.isVisible = true
            }
            DISCOUNT_CODE_APPLIED -> {
                rootView.applyDiscountCode.isVisible = false
                rootView.discountCodeAppliedLayout.isVisible = true
                rootView.discountCodeLayout.isVisible = false
            }
        }
    }
}
