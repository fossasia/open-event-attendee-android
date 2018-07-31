package org.fossasia.openevent.general.order

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import kotlin.collections.ArrayList

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersViewHolder>() {

    private val events = ArrayList<Event>()
    private var clickListener: OrderClickListener? = null
    private var orderIdentifier: String? = null

    fun setListener(listener: OrderClickListener) {
        clickListener = listener
    }

    fun addAll(orderList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(orderList)
    }

    fun setOrderIdentifier(orderId: String?) {
        orderIdentifier = orderId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_order, parent, false)
        return OrdersViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, clickListener, orderIdentifier)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    interface OrderClickListener {
        fun onClick(eventID: Long, orderIdentifier: String)
    }
}