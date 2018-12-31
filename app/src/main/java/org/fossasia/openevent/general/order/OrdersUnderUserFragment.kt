package org.fossasia.openevent.general.order

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_no_tickets.findMyTickets
import kotlinx.android.synthetic.main.fragment_orders_under_user.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersUnderUserCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.progressBar
import org.fossasia.openevent.general.AuthActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val ORDERS: String = "orders"
const val LAUNCH_TICKETS: String = "LAUNCH_TICKETS"

class OrdersUnderUserFragment : Fragment() {

    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserVM>()
    private val ordersRecyclerAdapter: OrdersRecyclerAdapter = OrdersRecyclerAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_orders_under_user, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.title = "Tickets"

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
                    val fragment = OrderDetailsFragment()
                    val bundle = Bundle()
                    bundle.putLong(EVENT_ID, eventID)
                    bundle.putString(ORDERS, orderIdentifier)
                    fragment.arguments = bundle
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.rootLayout, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }

            ordersRecyclerAdapter.setListener(recyclerViewClickListener)

            ordersUnderUserVM.progress
                .nonNull()
                .observe(this, Observer {
                    rootView.progressBar.isVisible = true
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
        } else {
            Snackbar.make(
                rootView.ordersUnderUserCoordinatorLayout, "You need to log in first!", Snackbar.LENGTH_SHORT
            ).show()
            Handler().postDelayed({
                redirectToLogin()
            }, 500)
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
        val authIntent = Intent(activity, AuthActivity::class.java)
        val redirectFromTickets = true
        authIntent.putExtra(LAUNCH_TICKETS, redirectFromTickets)
        startActivity(authIntent)
    }
}
