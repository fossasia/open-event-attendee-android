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
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
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

class ExpiredOrderFragment : Fragment() {
    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersPagedListAdapter: OrdersPagedListAdapter = OrdersPagedListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_expired_order, container, false)
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        ordersPagedListAdapter.setShowExpired(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager
        rootView.ordersRecycler.adapter = ordersPagedListAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        ordersUnderUserVM.showShimmerResults
            .nonNull()
            .observe(this, Observer {
                rootView.shimmerSearch.isVisible = it
                if (it) {
                    rootView.shimmerSearch.startShimmer()
                    showNoTicketsScreen(false)
                    showNoInternetScreen(false)
                } else {
                    rootView.shimmerSearch.stopShimmer()
                    showNoTicketsScreen(ordersPagedListAdapter.currentList?.isEmpty() ?: true)
                }
            })

        ordersUnderUserVM.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        ordersUnderUserVM.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                val currentItems = ordersUnderUserVM.eventAndOrderPaged.value
                if (currentItems != null) {
                    showNoInternetScreen(false)
                    showNoTicketsScreen(currentItems.size == 0)
                    ordersPagedListAdapter.submitList(currentItems)
                } else {
                    if (isConnected) {
                        ordersUnderUserVM.getOrdersAndEventsOfUser(true)
                    } else {
                        showNoInternetScreen(true)
                    }
                }
            })

        ordersUnderUserVM.eventAndOrderPaged
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                ordersPagedListAdapter.submitList(it)
            })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewClickListener = object : OrdersPagedListAdapter.OrderClickListener {
            override fun onClick(eventID: Long, orderIdentifier: String, orderId: Long) {
                findNavController(rootView).navigate(ExpiredOrderFragmentDirections
                    .actionOrderExpiredToOrderDetails(eventID, orderIdentifier, orderId))
            }
        }
        ordersPagedListAdapter.setListener(recyclerViewClickListener)
        rootView.filterToolbar.setOnClickListener {
            showFilterDialog()
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
            ordersUnderUserVM.getOrdersAndEventsOfUser(true)
        } else {
            showNoInternetScreen(true)
        }
    }

    private fun showNoInternetScreen(show: Boolean) {
        if (show) {
            rootView.shimmerSearch.isVisible = false
            showNoTicketsScreen(false)
            ordersPagedListAdapter.clear()
        }
        rootView.noInternetCard.isVisible = show
    }

    private fun showNoTicketsScreen(show: Boolean) {
        rootView.noTicketsScreen.isVisible = show
    }
}
