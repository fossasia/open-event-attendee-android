package org.fossasia.openevent.general.ticket

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_ticket.view.*
import org.fossasia.openevent.general.event.EventUtils

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(ticket: Ticket) {
        itemView.ticketName.text = ticket.name

        if (!ticket.minOrder.isNullOrEmpty() && !ticket.maxOrder.isNullOrEmpty()) {
            itemView.orderRange.text ="${ticket.minOrder}-${ticket.maxOrder}"
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