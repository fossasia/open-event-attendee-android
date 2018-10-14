package org.fossasia.openevent.general.order

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_order_details.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class OrderDetailsFragment : Fragment() {

    private lateinit var rootView: View
    private var id: Long = -1
    private lateinit var orderId: String
    private val orderDetailsViewModel by viewModel<OrderDetailsViewModel>()
    private val ordersRecyclerAdapter: OrderDetailsRecyclerAdapter = OrderDetailsRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
            orderId = bundle.getString(ORDERS)
        }
        ordersRecyclerAdapter.setOrderIdentifier(orderId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_order_details, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = ""
        setHasOptionsMenu(true)

        rootView.orderDetailsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.orderDetailsRecycler.adapter = ordersRecyclerAdapter
        rootView.orderDetailsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.orderDetailsRecycler.layoutManager = linearLayoutManager

        val eventDetailsListener = object : OrderDetailsRecyclerAdapter.EventDetailsListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.rootLayout, fragment)?.addToBackStack(null)?.commit()
            }
        }

        ordersRecyclerAdapter.setListener(eventDetailsListener)
        orderDetailsViewModel.event.observe(this, Observer {
            it?.let {
                ordersRecyclerAdapter.setEvent(it)
                ordersRecyclerAdapter.notifyDataSetChanged()
            }
        })

        orderDetailsViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBar, it) }
        })

        orderDetailsViewModel.attendees.observe(this, Observer {
            it?.let {
                ordersRecyclerAdapter.addAll(it)
                ordersRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched attendees of size %s", ordersRecyclerAdapter.itemCount)
        })

        orderDetailsViewModel.message.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        orderDetailsViewModel.loadEvent(id)
        orderDetailsViewModel.loadAttendeeDetails(orderId)

        return rootView
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

    override fun onDestroyView() {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        activity?.supportActionBar?.title = "Tickets"
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
