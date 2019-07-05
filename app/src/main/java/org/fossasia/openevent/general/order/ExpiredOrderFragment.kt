package org.fossasia.openevent.general.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_filter_order.view.completedOrdersCheckBox
import kotlinx.android.synthetic.main.dialog_filter_order.view.pendingOrdersCheckBox
import kotlinx.android.synthetic.main.dialog_filter_order.view.placedOrdersCheckBox
import kotlinx.android.synthetic.main.dialog_filter_order.view.dateRadioButton
import kotlinx.android.synthetic.main.dialog_filter_order.view.orderStatusRadioButton
import kotlinx.android.synthetic.main.fragment_expired_order.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_expired_order.view.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_expired_order.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_expired_order.view.filterToolbar
import kotlinx.android.synthetic.main.fragment_expired_order.view.toolbar
import org.fossasia.openevent.general.R
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
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        ordersRecyclerAdapter.setShowExpired(true)
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
                showNoTicketsScreen(it)
            })

        ordersUnderUserVM.eventAndOrder
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                ordersRecyclerAdapter.setSavedEventAndOrder(it)
                applyFilter()
                Timber.d("Fetched events of size %s", ordersRecyclerAdapter.itemCount)
            })

        val currentOrdersAndEvent = ordersUnderUserVM.eventAndOrder.value
        if (currentOrdersAndEvent == null) {
            ordersUnderUserVM.ordersUnderUser(true)
        } else {
            ordersRecyclerAdapter.setSavedEventAndOrder(currentOrdersAndEvent)
            applyFilter()
        }

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
        rootView.filterToolbar.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val filterLayout = layoutInflater.inflate(R.layout.dialog_filter_order, null)
        filterLayout.completedOrdersCheckBox.isChecked = ordersUnderUserVM.isShowingCompletedOrders
        filterLayout.pendingOrdersCheckBox.isChecked = ordersUnderUserVM.isShowingPendingOrders
        filterLayout.placedOrdersCheckBox.isChecked = ordersUnderUserVM.isShowingPlacedOrders
        if (ordersUnderUserVM.isSortingOrdersByDate)
            filterLayout.dateRadioButton.isChecked = true
        else
            filterLayout.orderStatusRadioButton.isChecked = true

        AlertDialog.Builder(requireContext())
            .setView(filterLayout)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.setPositiveButton(getString(R.string.apply)) { _, _ ->
                ordersUnderUserVM.isShowingCompletedOrders = filterLayout.completedOrdersCheckBox.isChecked
                ordersUnderUserVM.isShowingPendingOrders = filterLayout.pendingOrdersCheckBox.isChecked
                ordersUnderUserVM.isShowingPlacedOrders = filterLayout.placedOrdersCheckBox.isChecked
                ordersUnderUserVM.isSortingOrdersByDate = filterLayout.dateRadioButton.isChecked
                applyFilter()
            }.create().show()
    }

    private fun applyFilter() {
        ordersRecyclerAdapter.setFilter(
            completed = ordersUnderUserVM.isShowingCompletedOrders,
            placed = ordersUnderUserVM.isShowingPlacedOrders,
            pending = ordersUnderUserVM.isShowingPendingOrders,
            sortByDate = ordersUnderUserVM.isSortingOrdersByDate
        )
        showNoTicketsScreen(ordersRecyclerAdapter.itemCount == 0)
    }

    private fun showNoTicketsScreen(show: Boolean) {
        rootView.noTicketsScreen.isVisible = show
    }
}
