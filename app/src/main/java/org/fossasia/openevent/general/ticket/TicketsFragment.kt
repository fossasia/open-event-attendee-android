package org.fossasia.openevent.general.ticket

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_tickets.ticketsCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_tickets.view.eventName
import kotlinx.android.synthetic.main.fragment_tickets.view.organizerName
import kotlinx.android.synthetic.main.fragment_tickets.view.progressBarTicket
import kotlinx.android.synthetic.main.fragment_tickets.view.register
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketInfoTextView
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketTableHeader
import kotlinx.android.synthetic.main.fragment_tickets.view.ticketsRecycler
import kotlinx.android.synthetic.main.fragment_tickets.view.time
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.AttendeeFragmentArgs
import org.fossasia.openevent.general.auth.LoginFragmentArgs
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar

class TicketsFragment : Fragment() {
    private val ticketsRecyclerAdapter: TicketsRecyclerAdapter = TicketsRecyclerAdapter()
    private val ticketsViewModel by viewModel<TicketsViewModel>()
    private val safeArgs: TicketsFragmentArgs by navArgs()
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var ticketIdAndQty = ArrayList<Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticketsRecyclerAdapter.setCurrency(safeArgs.currency)

        val ticketSelectedListener = object : TicketSelectedListener {
            override fun onSelected(ticketId: Int, quantity: Int) {
                handleTicketSelect(ticketId, quantity)
                ticketsViewModel.ticketIdAndQty.value = ticketIdAndQty
            }
        }
        ticketsRecyclerAdapter.setSelectListener(ticketSelectedListener)

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tickets, container, false)
        setToolbar(activity, "Ticket Details")
        setHasOptionsMenu(true)

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        ticketsViewModel.progressTickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBarTicket.isVisible = it
                rootView.ticketTableHeader.isGone = it
                rootView.register.isGone = it
            })

        rootView.register.setOnClickListener {
            if (!ticketsViewModel.totalTicketsEmpty(ticketIdAndQty)) {
                checkForAuthentication()
            } else {
                handleNoTicketsSelected()
            }
        }

        ticketsViewModel.ticketTableVisibility
            .nonNull()
            .observe(viewLifecycleOwner, Observer { ticketTableVisible ->
                rootView.ticketTableHeader.isVisible = ticketTableVisible
                rootView.register.isVisible = ticketTableVisible
                rootView.ticketsRecycler.isVisible = ticketTableVisible
                rootView.ticketInfoTextView.isGone = ticketTableVisible
            })

        ticketsViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(ticketsCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        rootView.retry.setOnClickListener {
            loadTickets()
        }

        loadTickets()

        return rootView
    }

    private fun checkForAuthentication() {
        if (ticketsViewModel.isLoggedIn())
            redirectToAttendee()
        else {
            Snackbar.make(ticketsCoordinatorLayout, getString(R.string.log_in_first), Snackbar.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    private fun redirectToAttendee() {

        val wrappedTicketAndQty = TicketIdAndQtyWrapper(ticketIdAndQty)

        AttendeeFragmentArgs.Builder()
            .setTicketIdAndQty(wrappedTicketAndQty)
            .setEventId(safeArgs.eventId)
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.attendeeFragment, bundle, getAnimSlide())
            }
    }

    private fun redirectToLogin() {
        LoginFragmentArgs.Builder()
            .setSnackbarMessage(getString(R.string.log_in_first))
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.loginFragment, bundle, getAnimFade())
            }
    }

    private fun handleTicketSelect(id: Int, quantity: Int) {
        val pos = ticketIdAndQty.map { it.first }.indexOf(id)
        if (pos == -1) {
            ticketIdAndQty.add(Pair(id, quantity))
        } else {
            ticketIdAndQty[pos] = Pair(id, quantity)
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
        rootView.organizerName.text = "by ${event.organizerName.nullToEmpty()}"
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)
        rootView.time.text = EventUtils.getFormattedDateTimeRangeDetailed(startsAt, endsAt)
    }

    private fun handleNoTicketsSelected() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(resources.getString(R.string.no_tickets_message))
                .setTitle(resources.getString(R.string.whoops))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun loadTickets() {
        if (!isNetworkConnected(context) && ticketsViewModel.tickets.value.isNullOrEmpty())
            showNoInternetScreen(true)
        else {
            showNoInternetScreen(false)
            ticketsViewModel.loadEvent(safeArgs.eventId)
            ticketsViewModel.loadTickets(safeArgs.eventId)

            val retainedTicketIdAndQty: List<Pair<Int, Int>>? = ticketsViewModel.ticketIdAndQty.value
            if (retainedTicketIdAndQty != null) {
                for (idAndQty in retainedTicketIdAndQty) {
                    handleTicketSelect(idAndQty.first, idAndQty.second)
                }
                ticketsRecyclerAdapter.setTicketAndQty(retainedTicketIdAndQty)
                ticketsRecyclerAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.noInternetCard.isVisible = show
        rootView.ticketTableHeader.isVisible = !show
        rootView.ticketsRecycler.isVisible = !show
        rootView.progressBarTicket.isVisible = !show
        rootView.register.isVisible = !show
    }
}
