package org.fossasia.openevent.general.attendees


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_attendee.*
import kotlinx.android.synthetic.main.fragment_attendee.view.*

import org.fossasia.openevent.general.MainActivity

import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketId
import org.koin.android.architecture.ext.viewModel


class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private val EVENT_ID: String = "EVENT_ID"
    private val TICKET_ID: String = "TICKET_ID"
    private val attendeeFragmentViewModel by viewModel<AttendeeViewModel>()
    lateinit var event: Event
    lateinit var ticket: List<Ticket>
    private var ticketId: TicketId = TicketId(-1)
    private var eventId: EventId = EventId(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId.id = bundle.getLong(EVENT_ID, -1)
            ticketId.id = bundle.getLong(TICKET_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Attendee Details"
        setHasOptionsMenu(true)

        val attendee = AttendeeModel(1)
        rootView.register.setOnClickListener {
            attendee.email = email.text.toString()
            attendee.firstname = firstName.text.toString()
            attendee.lastname = lastName.text.toString()
            attendee.ticket = ticketId
            attendee.event = eventId
            attendeeFragmentViewModel.createAttendee(attendee)
        }

        return rootView
    }

    override fun onDestroyView() {
        val activity = activity as? MainActivity
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