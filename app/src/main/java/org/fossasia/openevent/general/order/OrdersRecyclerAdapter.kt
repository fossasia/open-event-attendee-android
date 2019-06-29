package org.fossasia.openevent.general.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.databinding.ItemCardOrderBinding

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersViewHolder>() {

    private val eventAndOrderIdentifier = ArrayList<Pair<Event, Order>>()
    private var showExpired = false
    private var clickListener: OrderClickListener? = null

    fun setListener(listener: OrderClickListener?) {
        clickListener = listener
    }

    fun addAllPairs(list: List<Pair<Event, Order>>, showExpired: Boolean) {
        if (eventAndOrderIdentifier.isNotEmpty())
            this.eventAndOrderIdentifier.clear()
        eventAndOrderIdentifier.addAll(list)
        this.showExpired = showExpired
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
