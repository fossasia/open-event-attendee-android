package org.fossasia.openevent.general.attendees


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_attendee.*
import kotlinx.android.synthetic.main.fragment_attendee.view.*

import org.fossasia.openevent.general.MainActivity

import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel


class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private val EVENT_ID: String = "EVENT_ID"
    private val TICKET_ID: String = "TICKET_ID"
    private val attendeeFragmentViewModel by viewModel<AttendeeViewModel>()
    lateinit var event: Event
    lateinit var ticket: List<Ticket>
    lateinit var ticketId: TicketId
    lateinit var eventId: EventId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = EventId(bundle.getLong(EVENT_ID, -1))
            ticketId=TicketId( bundle.getLong(TICKET_ID, -1))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Attendee Details"
        setHasOptionsMenu(true)

        attendeeFragmentViewModel.message.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        rootView.register.setOnClickListener {
            val attendee = Attendee(attendeeFragmentViewModel.id
                    ,firstName.text.toString(),lastName.text.toString(),email.text.toString(),ticket = ticketId,event = eventId)

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