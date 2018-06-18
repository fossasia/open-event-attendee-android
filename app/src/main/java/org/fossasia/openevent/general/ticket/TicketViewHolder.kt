package org.fossasia.openevent.general.ticket

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_ticket.view.*
import org.fossasia.openevent.general.event.EventUtils

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(ticket: Ticket) {
        if (!ticket.description.isNullOrEmpty()) {
            itemView.ticketDescription.text = ticket.description
        }
        itemView.ticketName.text = ticket.name

        if(ticket.salesEndsAt != null && ticket.salesStartsAt != null) {
            val salesStartsAt = EventUtils.getLocalizedDateTime(ticket.salesStartsAt)
            val salesEndsAt = EventUtils.getLocalizedDateTime(ticket.salesEndsAt)

            itemView.salesStartsAt.text = "${salesStartsAt.dayOfMonth} ${salesStartsAt.month} ${salesStartsAt.year}"
            itemView.salesEndsAt.text = "${salesEndsAt.dayOfMonth} ${salesEndsAt.month} ${salesEndsAt.year}"
        }

        if (!ticket.maxOrder.isNullOrEmpty()) {
            itemView.maxOrder.text = ticket.maxOrder
            itemView.orderRange.visibility = View.VISIBLE
        }

        if (!ticket.minOrder.isNullOrEmpty()) {
            itemView.minimumOrder.text = ticket.minOrder
            itemView.orderRange.visibility = View.VISIBLE
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