package org.fossasia.openevent.general.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_expired_order.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_expired_order.view.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_expired_order.view.shimmerSearch
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.longSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ExpiredOrderFragment : Fragment() {
    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersRecyclerAdapter: OrdersRecyclerAdapter = OrdersRecyclerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_expired_order, container, false)
        setToolbar(activity, getString(R.string.past_tickets))
        setHasOptionsMenu(true)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager
        rootView.ordersRecycler.adapter = ordersRecyclerAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        ordersUnderUserVM.showShimmerResults
            .nonNull()
            .observe(this, Observer {
                rootView.shimmerSearch.isVisible = it
            })

        ordersUnderUserVM.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        ordersUnderUserVM.noTickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.noTicketsScreen.isVisible = it
            })

        ordersUnderUserVM.eventAndOrder
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                val list = it.sortedByDescending {
                    EventUtils.getTimeInMilliSeconds(it.first.startsAt, null)
                }
                ordersRecyclerAdapter.addAllPairs(list, true)
                ordersRecyclerAdapter.notifyDataSetChanged()
                Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
            })

        ordersUnderUserVM.ordersUnderUser(true)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewClickListener = object : OrdersRecyclerAdapter.OrderClickListener {
            override fun onClick(eventID: Long, orderIdentifier: String, orderId: Long) {
                findNavController(rootView).navigate(ExpiredOrderFragmentDirections
                    .actionOrderExpiredToOrderDetails(eventID, orderIdentifier, orderId))
            }
        }
        ordersRecyclerAdapter.setListener(recyclerViewClickListener)
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
