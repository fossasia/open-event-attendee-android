package org.fossasia.openevent.general.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_order_details.view.orderDetailCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_order_details.view.orderDetailsRecycler
import kotlinx.android.synthetic.main.fragment_order_details.view.progressBar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar

class OrderDetailsFragment : Fragment() {

    private lateinit var rootView: View
    private val orderDetailsViewModel by viewModel<OrderDetailsViewModel>()
    private val ordersRecyclerAdapter: OrderDetailsRecyclerAdapter = OrderDetailsRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val safeArgs: OrderDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ordersRecyclerAdapter.setOrderIdentifier(safeArgs.orders)

        orderDetailsViewModel.event
            .nonNull()
            .observe(this, Observer {
                ordersRecyclerAdapter.setEvent(it)
                ordersRecyclerAdapter.notifyDataSetChanged()
            })

        orderDetailsViewModel.attendees
            .nonNull()
            .observe(this, Observer {
                ordersRecyclerAdapter.addAll(it)
                ordersRecyclerAdapter.notifyDataSetChanged()
                Timber.d("Fetched attendees of size %s", ordersRecyclerAdapter.itemCount)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_order_details, container, false)
        setToolbar(activity)
        setHasOptionsMenu(true)

        rootView.orderDetailsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.orderDetailsRecycler.adapter = ordersRecyclerAdapter
        rootView.orderDetailsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.orderDetailsRecycler.layoutManager = linearLayoutManager
        LinearSnapHelper().attachToRecyclerView(rootView.orderDetailsRecycler)

        val eventDetailsListener = object : OrderDetailsRecyclerAdapter.EventDetailsListener {
            override fun onClick(eventID: Long) {
                EventDetailsFragmentArgs.Builder()
                    .setEventId(eventID)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        findNavController(rootView).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
                    }
            }
        }

        ordersRecyclerAdapter.setListener(eventDetailsListener)

        orderDetailsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        orderDetailsViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView.orderDetailCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        orderDetailsViewModel.loadEvent(safeArgs.eventId)
        orderDetailsViewModel.loadAttendeeDetails(safeArgs.orders)

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
}
