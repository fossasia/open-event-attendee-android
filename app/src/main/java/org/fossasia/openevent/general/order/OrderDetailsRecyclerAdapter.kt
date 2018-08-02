package org.fossasia.openevent.general.order

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.event.Event
import kotlin.collections.ArrayList

class OrderDetailsRecyclerAdapter : RecyclerView.Adapter<OrderDetailsViewHolder>() {

    private val orders = ArrayList<Attendee>()
    private var event: Event? = null
    private var orderIdentifier: String? = null

    fun addAll(orderList: List<Attendee>) {
        if (orders.isNotEmpty())
            this.orders.clear()
        this.orders.addAll(orderList)
    }

    fun setEvent(event: Event?) {
        this.event = event
    }

    fun setOrderIdentifier(orderId: String?) {
        orderIdentifier = orderId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_order_details, parent, false)
        return OrderDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order, event, orderIdentifier)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}