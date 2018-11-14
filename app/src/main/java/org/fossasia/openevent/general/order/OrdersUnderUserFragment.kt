package org.fossasia.openevent.general.order

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.content_no_tickets.*
import kotlinx.android.synthetic.main.fragment_orders_under_user.*
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.*
import org.fossasia.openevent.general.AuthActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
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
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
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
                    activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.rootLayout, fragment)?.addToBackStack(null)?.commit()
                }
            }

            ordersRecyclerAdapter.setListener(recyclerViewClickListener)

            ordersUnderUserVM.progress.observe(this, Observer {
                it?.let { Utils.showProgressBar(rootView.progressBar, it) }
            })

            ordersUnderUserVM.message.observe(this, Observer {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            })

            ordersUnderUserVM.noTickets.observe(this, Observer {
                it?.let { showNoTicketsScreen(it) }
            })

            ordersUnderUserVM.attendeesNumber.observe(this, Observer {
                it?.let {
                    ordersRecyclerAdapter.setAttendeeNumber(it)
                }
            })

            ordersUnderUserVM.eventAndOrderIdentifier.observe(this, Observer {
                it?.let {
                    ordersRecyclerAdapter.addAllPairs(it)
                    ordersRecyclerAdapter.notifyDataSetChanged()
                }
                Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
            })
        } else {
            redirectToLogin()
            Toast.makeText(context, "You need to log in first!", Toast.LENGTH_LONG).show()
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
