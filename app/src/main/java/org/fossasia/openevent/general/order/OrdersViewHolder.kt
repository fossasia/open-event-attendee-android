package org.fossasia.openevent.general.order

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_card_order.view.*
import org.fossasia.openevent.general.event.EventUtils

class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(order: Order,clickListener: OrdersRecyclerAdapter.OrderClickListener?) {
        val formattedDate = EventUtils.getLocalizedDateTime(order.createdAt.toString())
        itemView.paymentMode.text = order.paymentMode
        itemView.orderIdentier.text = order.identifier
        itemView.time.text = EventUtils.getFormattedDateShort(formattedDate)
        itemView.setOnClickListener {
            order.identifier?.let { it1 -> order.event?.id?.let { it2 -> clickListener?.onClick(it2, it1) } }
        }
    }
}