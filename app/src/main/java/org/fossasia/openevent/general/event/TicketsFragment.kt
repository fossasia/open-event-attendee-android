package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_tickets.view.*
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel

class TicketsFragment : Fragment() {
    private val ticketsRecyclerAdapter: TicketsRecyclerAdapter = TicketsRecyclerAdapter()
    private val ticketsViewModel by viewModel<TicketsViewModel>()
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_tickets, container, false)

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        ticketsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        ticketsViewModel.progressTickets.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        ticketsViewModel.loadTickets(id)

        ticketsViewModel.tickets.observe(this, Observer {
            it?.let {
                ticketsRecyclerAdapter.addAll(it)
            }
            ticketsRecyclerAdapter.notifyDataSetChanged()
        })

        //set event id
        ticketsRecyclerAdapter.setEventId(id)

        return rootView
    }

    private fun showProgressBar(show: Boolean) {
        rootView.progressBarTicket.isIndeterminate = show
        rootView.progressBarTicket.visibility = if (show) View.VISIBLE else View.GONE
    }
}