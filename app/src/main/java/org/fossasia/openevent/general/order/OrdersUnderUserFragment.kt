package org.fossasia.openevent.general.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_no_tickets.findMyTickets
import kotlinx.android.synthetic.main.fragment_orders_under_user.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersUnderUserCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.progressBar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.SNACKBAR_MESSAGE
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val ORDERS: String = "orders"

class OrdersUnderUserFragment : Fragment() {

    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserVM>()
    private val ordersRecyclerAdapter: OrdersRecyclerAdapter = OrdersRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager

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

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = "Tickets"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        rootView.ordersRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.ordersRecycler.adapter = ordersRecyclerAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager

        if (ordersUnderUserVM.isLoggedIn()) {
            ordersUnderUserVM.ordersUnderUser()

            val recyclerViewClickListener = object : OrdersRecyclerAdapter.OrderClickListener {
                override fun onClick(eventID: Long, orderIdentifier: String) {
                    val bundle = Bundle()
                    bundle.putLong(EVENT_ID, eventID)
                    bundle.putString(ORDERS, orderIdentifier)
                    findNavController(rootView).navigate(R.id.orderDetailsFragment, bundle, getAnimFade())
                }
            }

            ordersRecyclerAdapter.setListener(recyclerViewClickListener)

            ordersUnderUserVM.progress
                .nonNull()
                .observe(this, Observer {
                    rootView.progressBar.isVisible = it
                })

            ordersUnderUserVM.message
                .nonNull()
                .observe(this, Observer {
                    Snackbar.make(
                        rootView.ordersUnderUserCoordinatorLayout, it, Snackbar.LENGTH_LONG
                    ).show()
                })

            ordersUnderUserVM.noTickets
                .nonNull()
                .observe(this, Observer {
                    showNoTicketsScreen(it)
                })

            ordersUnderUserVM.attendeesNumber
                .nonNull()
                .observe(this, Observer {
                    ordersRecyclerAdapter.setAttendeeNumber(it)
                })

            ordersUnderUserVM.eventAndOrderIdentifier
                .nonNull()
                .observe(this, Observer {
                    ordersRecyclerAdapter.addAllPairs(it)
                    ordersRecyclerAdapter.notifyDataSetChanged()
                    Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
                })
        }

        return rootView
    }

    private fun showNoTicketsScreen(show: Boolean) {
        noTicketsScreen.visibility = if (show) View.VISIBLE else View.GONE
        findMyTickets.setOnClickListener {
            context?.let {
                Utils.openUrl(it, resources.getString(R.string.ticket_issues_url))
            }
        }
    }

    private fun redirectToLogin() {
        val args = getString(R.string.log_in_first)
        val bundle = bundleOf(SNACKBAR_MESSAGE to args)
        findNavController(rootView).navigate(R.id.loginFragment, bundle, getAnimFade())
    }
}
