package org.fossasia.openevent.general.order

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import kotlin.collections.ArrayList

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersViewHolder>() {

    private val orders = ArrayList<Order>()

    fun addAll(orderList: List<Order>) {
        if (orders.isNotEmpty())
            this.orders.clear()
        this.orders.addAll(orderList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_order, parent, false)
        return OrdersViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

}