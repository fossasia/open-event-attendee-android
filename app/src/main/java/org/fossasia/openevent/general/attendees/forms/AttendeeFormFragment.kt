package org.fossasia.openevent.general.attendees.forms

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_attendee_forms.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class AttendeeFormFragment : Fragment() {
    private val attendeeFormRecyclerAdapter: AttendeeFormRecyclerAdapter = AttendeeFormRecyclerAdapter()
    private val attendeeFormViewModel by viewModel<AttendeeFormViewModel>()
    private var ticketDetailsAndQty: List<Triple<String, Int, Int>>? = null
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            ticketDetailsAndQty = bundle.getSerializable(TICKET_ID_AND_QTY) as List<Triple<String, Int, Int>>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee_forms, container, false)

        rootView.attendeeFormRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.attendeeFormRecycler.adapter = attendeeFormRecyclerAdapter
        rootView.attendeeFormRecycler.isNestedScrollingEnabled = false

        attendeeFormViewModel.attendeeFormData.observe(this, Observer {
            Timber.d("Fetched attendee Forms of size %s", attendeeFormRecyclerAdapter.itemCount)
            it?.let { it1 ->
                attendeeFormRecyclerAdapter.addAll(it1)
            }
        })

        attendeeFormViewModel.ticketDetails(ticketDetailsAndQty)

        return rootView
    }
}