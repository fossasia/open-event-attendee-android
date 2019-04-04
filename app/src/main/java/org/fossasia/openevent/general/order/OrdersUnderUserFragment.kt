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
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.navigation
import kotlinx.android.synthetic.main.content_no_tickets.findMyTickets
import kotlinx.android.synthetic.main.content_no_tickets.noTicketMessage
import kotlinx.android.synthetic.main.fragment_orders_under_user.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersUnderUserCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersNestedScrollView
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.expireFilter
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.ScrollToTop
import org.fossasia.openevent.general.auth.LoginFragmentArgs
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.Utils.navAnimGone
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar

class OrdersUnderUserFragment : Fragment(), ScrollToTop {

    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersRecyclerAdapter: OrdersRecyclerAdapter = OrdersRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val safeArgs: OrdersUnderUserFragmentArgs by navArgs()

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
        when (safeArgs.showExpired) {
            true -> {
                setToolbar(activity, "Past Tickets")
                setHasOptionsMenu(true)
                navAnimGone(activity?.navigation, requireContext())
            }
            false -> {
                setToolbar(activity, getString(R.string.tickets), false)
            }
        }

        rootView.ordersRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ordersRecycler.adapter = ordersRecyclerAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager

        if (ordersUnderUserVM.isLoggedIn()) {
            if (ordersRecyclerAdapter.itemCount == 0) ordersUnderUserVM.ordersUnderUser(safeArgs.showExpired)
            if (safeArgs.showExpired) rootView.expireFilter.isVisible = false

            val recyclerViewClickListener = object : OrdersRecyclerAdapter.OrderClickListener {
                override fun onClick(eventID: Long, orderIdentifier: String) {
                    OrderDetailsFragmentArgs.Builder()
                        .setEventId(eventID)
                        .setOrders(orderIdentifier)
                        .build()
                        .toBundle()
                        .also { bundle ->
                            findNavController(rootView).navigate(R.id.orderDetailsFragment, bundle, getAnimFade())
                        }
                }
            }

            rootView.expireFilter.setOnClickListener {
                OrdersUnderUserFragmentArgs.Builder()
                    .setShowExpired(true)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        findNavController(rootView).navigate(R.id.orderUnderUserFragment, bundle, getAnimSlide())
                    }
            }

            ordersRecyclerAdapter.setListener(recyclerViewClickListener)

            ordersUnderUserVM.showShimmerResults
                .nonNull()
                .observe(this, Observer {
                    rootView.shimmerSearch.isVisible = it
                })

            ordersUnderUserVM.message
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    Snackbar.make(
                        rootView.ordersUnderUserCoordinatorLayout, it, Snackbar.LENGTH_LONG
                    ).show()
                })

            ordersUnderUserVM.noTickets
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    showNoTicketsScreen(it)
                })

            ordersUnderUserVM.attendeesNumber
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    ordersRecyclerAdapter.setAttendeeNumber(it)
                })

            ordersUnderUserVM.eventAndOrderIdentifier
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    val list = it.sortedByDescending {
                        EventUtils.getTimeInMilliSeconds(it.first.startsAt, null)
                    }
                    ordersRecyclerAdapter.addAllPairs(list, safeArgs.showExpired)
                    ordersRecyclerAdapter.notifyDataSetChanged()
                    Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
                })
        }

        return rootView
    }

    private fun showNoTicketsScreen(show: Boolean) {
        noTicketsScreen.isVisible = show
        if (safeArgs.showExpired) {
            findMyTickets.isVisible = false
            noTicketMessage.text = getString(R.string.no_past_tickets)
        } else {
            findMyTickets.setOnClickListener {
                Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
            }
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

    override fun scrollToTop() = rootView.ordersNestedScrollView.smoothScrollTo(0, 0)

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
