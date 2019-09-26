package org.fossasia.openevent.general.order

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation.findNavController
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.ShippingAddress
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.android.sdk.payments.PaymentConfirmation
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.dialog_filter_order.view.orderStatusRadioButton
import kotlinx.android.synthetic.main.dialog_filter_order.view.dateRadioButton
import kotlinx.android.synthetic.main.dialog_filter_order.view.completedOrdersCheckBox
import kotlinx.android.synthetic.main.dialog_filter_order.view.pendingOrdersCheckBox
import kotlinx.android.synthetic.main.dialog_filter_order.view.placedOrdersCheckBox
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.findMyTickets
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.swipeRefresh
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.scrollView
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.pastEvent
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsNumber
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.toolbarLayout
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsTitle
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.filterToolbar
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.filter
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.BottomIconDoubleClick
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.ticket.TicketIdAndQtyWrapper
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.hideWithFading
import org.fossasia.openevent.general.utils.extensions.showWithFading
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import java.math.BigDecimal
import java.util.Currency

const val ORDERS_FRAGMENT = "ordersFragment"
private const val PAYPAL_REQUEST_CODE = 401


class OrdersUnderUserFragment : Fragment(), BottomIconDoubleClick {


    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersPagedListAdapter = OrdersPagedListAdapter()
    private val mutableMessage = SingleLiveEvent<String>()
    val paypalMessage: LiveData<String> = mutableMessage
    private lateinit var pendingOrder: Order
    private var ticketIdAndQty = ArrayList<Triple<Int, Int, Float>>()


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

        ordersPagedListAdapter.setShowExpired(false)
        rootView.ordersRecycler.adapter = ordersPagedListAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager

        ordersUnderUserVM.connection
                .nonNull()
                .observe(viewLifecycleOwner, Observer { isConnected ->
                    val currentItems = ordersUnderUserVM.eventAndOrderPaged.value
                    if (currentItems != null) {
                        showNoInternetScreen(false)
                        showNoTicketsScreen(currentItems.size == 0)
                        ordersPagedListAdapter.submitList(currentItems)
                    } else {
                        ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = true)
                    }
                })

        ordersUnderUserVM.numOfTickets
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    rootView.ticketsNumber.text = resources.getQuantityString(R.plurals.numOfOrders, it, it)
                    showNoTicketsScreen(it == 0 && !rootView.shimmerSearch.isVisible)
                })

        ordersUnderUserVM.showShimmerResults
                .nonNull()
                .observe(this, Observer {
                    if (it) {
                        rootView.shimmerSearch.startShimmer()
                        showNoTicketsScreen(false)
                        showNoInternetScreen(false)
                    } else {
                        rootView.shimmerSearch.stopShimmer()
                        rootView.swipeRefresh.isRefreshing = false
                    }
                    rootView.shimmerSearch.isVisible = it
                })

        ordersUnderUserVM.message
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    rootView.longSnackbar(it)
                    ordersUnderUserVM.clearOrders()
                    ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = false)
                })

        ordersUnderUserVM.eventAndOrderPaged
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    ordersPagedListAdapter.submitList(it)
                })

        rootView.swipeRefresh.setColorSchemeColors(Color.BLUE)
        rootView.swipeRefresh.setOnRefreshListener {
            if (ordersUnderUserVM.isConnected()) {
                ordersUnderUserVM.clearOrders()
                ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = false)
            } else {
                rootView.swipeRefresh.isRefreshing = false
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewClickListener = object : OrdersPagedListAdapter.OrderClickListener {
            override fun onClick(eventID: Long, orderIdentifier: String, orderId: Long, event: Event, order: Order) {
                if (order.status.equals("completed") || order.status.equals("placed")) {
                    findNavController(rootView).navigate(OrdersUnderUserFragmentDirections
                            .actionOrderUserToOrderDetails(eventID, orderIdentifier, orderId))
                } else if (order.status.equals("cancelled")) {
                    rootView.snackbar("Event is Cancelled")
                } else if (order.status.equals("expired")) {
                    rootView.snackbar("Event is Expired")
                } else if (order.status.equals("pending")) {
                    if (event.canPayByPaypal) {
                        pendingOrder = order
                        startPaypalPayment(event, order)
                    } else if (event.canPayByStripe) {
                        navigateToAttendeeFragment(event, order)
                    }
                }
            }

        }
        ordersPagedListAdapter.setListener(recyclerViewClickListener)

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
        rootView.filter.setOnClickListener {
            showFilterDialog()
        }
        rootView.filterToolbar.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun navigateToAttendeeFragment(event: Event, order: Order) {

        getTicketIdAndQty(event, order, 0F)
        val ticketIdAndQtyWrapper = TicketIdAndQtyWrapper(ticketIdAndQty)
        findNavController(rootView).navigate(OrdersUnderUserFragmentDirections.actionOrderUserToAttendee(event.id, ticketIdAndQtyWrapper, event.paymentCurrency, order.amount, 0F))

    }


    private fun getTicketIdAndQty(event: Event, order: Order, fl: Float) {

        ticketIdAndQty.let { ticketIdAndQty ->
            order.attendees.forEach {

                ticketIdAndQty.add(Triple(it.id.toInt(), order.attendees.size, fl))

            }
        }


    }

    private fun startPaypalPayment(event: Event, order: Order) {

        val paypalEnvironment = if (BuildConfig.DEBUG) PayPalConfiguration.ENVIRONMENT_SANDBOX
        else PayPalConfiguration.ENVIRONMENT_PRODUCTION
        val paypalConfig = PayPalConfiguration()
                .environment(paypalEnvironment)
                .clientId(BuildConfig.PAYPAL_CLIENT_ID)
        val paypalIntent = Intent(activity, PaymentActivity::class.java)
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        activity?.startService(paypalIntent)

        val paypalPayment = paypalThingsToBuy(PayPalPayment.PAYMENT_INTENT_SALE, order, event)
        val payeeEmail = event.paypalEmail ?: " "
        paypalPayment.payeeEmail(payeeEmail)
        addShippingAddress(paypalPayment, event, order)
        val intent = Intent(activity, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
        startActivityForResult(intent, PAYPAL_REQUEST_CODE)


    }

    private fun addShippingAddress(paypalPayment: PayPalPayment, event: Event, order: Order) {
        if (order.isBillingEnabled) {
            val shippingAddress = ShippingAddress()
                    .recipientName("${order.attendees[0].firstname} ${order.attendees[0].lastname}")
                    .line1(order.address)
                    .city(order.city)
                    .state(order.state)
                    .postalCode(order.zipcode)
                    .countryCode(getCountryCodes(order.country.toString()))
            paypalPayment.providedShippingAddress(shippingAddress)
        }
    }

    private fun getCountryCodes(countryName: String): String {
        val countryCodes = resources.getStringArray(R.array.country_code_arrays)
        val countryList = resources.getStringArray(R.array.country_arrays)
        val index = countryList.indexOf(countryName)
        return countryCodes[index]
    }

    private fun paypalThingsToBuy(paymentIntent: String, order: Order, event: Event): PayPalPayment =
            PayPalPayment(BigDecimal(order.amount.toString()),
                    Currency.getInstance(event.paymentCurrency).currencyCode,
                    getString(R.string.tickets_for, event.name), paymentIntent)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PAYPAL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val paymentConfirm =
                    data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
            if (paymentConfirm != null) {
                val paymentId = paymentConfirm.proofOfPayment.paymentId
                ordersUnderUserVM.sendPaypalConfirm(paymentId, pendingOrder)
            }
        }
    }

    private fun showFilterDialog() {
        val filterLayout = layoutInflater.inflate(R.layout.dialog_filter_order, null)
        filterLayout.completedOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingCompletedOrders
        filterLayout.pendingOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingPendingOrders
        filterLayout.placedOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingPlacedOrders
        if (ordersUnderUserVM.filter.isSortingOrdersByDate)
            filterLayout.dateRadioButton.isChecked = true
        else
            filterLayout.orderStatusRadioButton.isChecked = true

        AlertDialog.Builder(requireContext())
                .setView(filterLayout)
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }.setPositiveButton(getString(R.string.apply)) { _, _ ->
                    ordersUnderUserVM.filter.isShowingCompletedOrders = filterLayout.completedOrdersCheckBox.isChecked
                    ordersUnderUserVM.filter.isShowingPendingOrders = filterLayout.pendingOrdersCheckBox.isChecked
                    ordersUnderUserVM.filter.isShowingPlacedOrders = filterLayout.placedOrdersCheckBox.isChecked
                    ordersUnderUserVM.filter.isSortingOrdersByDate = filterLayout.dateRadioButton.isChecked
                    applyFilter()
                }.create().show()
    }

    private fun applyFilter() {
        ordersUnderUserVM.clearOrders()
        ordersPagedListAdapter.clear()
        if (ordersUnderUserVM.isConnected()) {
            ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = false)
        } else {
            showNoInternetScreen(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView.swipeRefresh.setOnRefreshListener(null)
        ordersPagedListAdapter.setListener(null)
    }

    private fun showNoTicketsScreen(show: Boolean) {
        rootView.noTicketsScreen.isVisible = show
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(OrdersUnderUserFragmentDirections
                .actionOrderUserToAuth(getString(R.string.log_in_first), ORDERS_FRAGMENT))
    }

    private fun showNoInternetScreen(show: Boolean) {
        if (show) {
            rootView.shimmerSearch.isVisible = false
            showNoTicketsScreen(false)
            ordersPagedListAdapter.clear()
        }
        rootView.noInternetCard.isVisible = show
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
