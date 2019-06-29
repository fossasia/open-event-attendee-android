package org.fossasia.openevent.general.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.findMyTickets
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.scrollView
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.pastEvent
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsNumber
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.toolbarLayout
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsTitle
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.BottomIconDoubleClick
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.hideWithFading
import org.fossasia.openevent.general.utils.extensions.showWithFading
import org.jetbrains.anko.design.longSnackbar

const val ORDERS_FRAGMENT = "ordersFragment"

class OrdersUnderUserFragment : Fragment(), BottomIconDoubleClick {

    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersRecyclerAdapter: OrdersRecyclerAdapter = OrdersRecyclerAdapter()

    override fun onStart() {
        super.onStart()
        if (!ordersUnderUserVM.isLoggedIn()) {
            redirectToLogin()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_orders_under_user, container, false)
        setToolbar(activity, show = false)

        rootView.ordersRecycler.adapter = ordersRecyclerAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager

        if (ordersUnderUserVM.eventAndOrder.value == null)
            ordersUnderUserVM.ordersUnderUser(false)

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
                showNoTicketsScreen(it)
            })

        ordersUnderUserVM.eventAndOrder
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                val list = it.sortedByDescending {
                    EventUtils.getTimeInMilliSeconds(it.first.startsAt, null)
                }
                rootView.ticketsNumber.text = "${it.size} orders"
                ordersRecyclerAdapter.addAllPairs(list, false)
                ordersRecyclerAdapter.notifyDataSetChanged()
                Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
            })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewClickListener = object : OrdersRecyclerAdapter.OrderClickListener {
            override fun onClick(eventID: Long, orderIdentifier: String, orderId: Long) {
                findNavController(rootView).navigate(OrdersUnderUserFragmentDirections
                    .actionOrderUserToOrderDetails(eventID, orderIdentifier, orderId))
            }
        }
        ordersRecyclerAdapter.setListener(recyclerViewClickListener)

        rootView.pastEvent.setOnClickListener {
            findNavController(rootView).navigate(OrdersUnderUserFragmentDirections.actionOrderUserToOrderExpired())
        }
        rootView.findMyTickets.setOnClickListener {
            Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
        }
        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.ticketsTitle.y && !rootView.toolbarLayout.isVisible) {
                rootView.toolbarLayout.showWithFading()
            } else if (scrollY < rootView.ticketsTitle.y && rootView.toolbarLayout.isVisible) {
                rootView.toolbarLayout.hideWithFading()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersRecyclerAdapter.setListener(null)
    }

    private fun showNoTicketsScreen(show: Boolean) {
        rootView.noTicketsScreen.isVisible = show
        if (show) rootView.ticketsNumber.text = getString(R.string.no_tickets)
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(OrdersUnderUserFragmentDirections
            .actionOrderUserToAuth(getString(R.string.log_in_first), ORDERS_FRAGMENT))
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)

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
