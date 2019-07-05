package org.fossasia.openevent.general.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.attendees.ORDER_STATUS_COMPLETED
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PENDING
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PLACED
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.databinding.ItemCardOrderBinding
import org.fossasia.openevent.general.event.EventUtils

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersViewHolder>() {

    private val savedEventAndOrder = ArrayList<Pair<Event, Order>>()
    private val eventAndOrderIdentifier = ArrayList<Pair<Event, Order>>()
    private var showExpired = false
    private var clickListener: OrderClickListener? = null

    private var isShowingCompletedOrders = true
    private var isShowingPendingOrders = true
    private var isShowingPlacedOrders = true
    private var isSortingOrdersByDate = true

    fun setListener(listener: OrderClickListener?) {
        clickListener = listener
    }

    fun setShowExpired(expired: Boolean) {
        showExpired = expired
    }

    fun setSavedEventAndOrder(list: List<Pair<Event, Order>>) {
        if (savedEventAndOrder.isNotEmpty())
            savedEventAndOrder.clear()
        savedEventAndOrder.addAll(list)
    }

    private fun addAllPairs() {
        if (eventAndOrderIdentifier.isNotEmpty())
            this.eventAndOrderIdentifier.clear()
        val filteredList = ArrayList<Pair<Event, Order>>()
        if (isShowingCompletedOrders) {
            filteredList.addAll(savedEventAndOrder.filter { it.second.status == ORDER_STATUS_COMPLETED })
        }
        if (isShowingPendingOrders) {
            filteredList.addAll(savedEventAndOrder.filter { it.second.status == ORDER_STATUS_PENDING })
        }
        if (isShowingPlacedOrders) {
            filteredList.addAll(savedEventAndOrder.filter { it.second.status == ORDER_STATUS_PLACED })
        }
        if (isSortingOrdersByDate) {
            filteredList.sortedByDescending { eventAndOrder ->
                EventUtils.getTimeInMilliSeconds(eventAndOrder.first.startsAt, null)
            }.also { eventAndOrderIdentifier.addAll(it) }
        } else {
            filteredList.sortedBy { eventAndOrder ->
                eventAndOrder.second.status
            }.also {
                eventAndOrderIdentifier.addAll(it)
            }
        }
        notifyDataSetChanged()
    }

    fun setFilter(completed: Boolean, pending: Boolean, placed: Boolean, sortByDate: Boolean) {
        isShowingPlacedOrders = placed
        isShowingPendingOrders = pending
        isShowingCompletedOrders = completed
        isSortingOrdersByDate = sortByDate
        addAllPairs()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = ItemCardOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.bind(eventAndOrderIdentifier[position], showExpired, clickListener)
    }

    override fun getItemCount(): Int {
        return eventAndOrderIdentifier.size
    }

    interface OrderClickListener {
        fun onClick(eventID: Long, orderIdentifier: String, orderId: Long)
    }
}
