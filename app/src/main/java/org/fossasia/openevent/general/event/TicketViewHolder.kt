package org.fossasia.openevent.general.event

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_ticket.view.*
import org.fossasia.openevent.general.utils.nullToEmpty

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(ticket: Ticket, eventId: Long) {
        if (!ticket.description.isNullOrEmpty()) {
            itemView.ticket_description.text = ticket.description
        }
        itemView.ticket_name.text = ticket.name

        ticket.eventId = eventId

        if(ticket.salesEndsAt != null && ticket.salesStartsAt != null) {
            val salesStartsAt = EventUtils.getLocalizedDateTime(ticket.salesStartsAt)
            val salesEndsAt = EventUtils.getLocalizedDateTime(ticket.salesEndsAt)

            itemView.sales_starts_at.text = "${salesStartsAt.dayOfMonth} ${salesStartsAt.month} ${salesStartsAt.year}"
            itemView.sales_ends_at.text = "${salesEndsAt.dayOfMonth} ${salesEndsAt.month} ${salesEndsAt.year}"
        }

        if (!ticket.maxOrder.isNullOrEmpty()) {
            itemView.max_order.text = ticket.maxOrder
            itemView.order_range.visibility = View.VISIBLE
        }

        if (!ticket.minOrder.isNullOrEmpty()) {
            itemView.minimum_order.text = ticket.minOrder
            itemView.order_range.visibility = View.VISIBLE
        }

        if (!ticket.quantity.isNullOrEmpty()) {
            itemView.quantity.text = "${ticket.quantity} tickets"
        }

        if (!ticket.price.isNullOrEmpty()) {
            itemView.price.visibility = View.VISIBLE
            itemView.price.text = "$${ticket.price}"
        }

        if (ticket.price.equals("0.0")) {
            itemView.price.text = "Free"
        }
    }
}