package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_ticket_details.view.*

class TicketDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(ticket: Ticket, qty: ArrayList<Int>, eventCurrency: String?) {
        itemView.ticketName.text = ticket.name

        if (ticket.price != null) {
            itemView.price.visibility = View.VISIBLE
            itemView.price.text = "$${ticket.price}"
        }

        if (ticket.price != null) {
            itemView.price.text = "$eventCurrency${ticket.price}"
        }

        if (ticket.price == 0.toFloat()) {
            itemView.price.text = "Free"
        }

        val subTotal: Float? = ticket.price?.times(qty[adapterPosition])
        itemView.qty.text = qty[adapterPosition].toString()
        itemView.subTotal.text = "$$subTotal"
    }
}
