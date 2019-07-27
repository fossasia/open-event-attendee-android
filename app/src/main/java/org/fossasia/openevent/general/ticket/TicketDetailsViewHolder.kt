package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_ticket_details.view.ticketName
import kotlinx.android.synthetic.main.item_ticket_details.view.price
import kotlinx.android.synthetic.main.item_ticket_details.view.qty
import kotlinx.android.synthetic.main.item_ticket_details.view.subTotal
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource

class TicketDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val resource = Resource()

    fun bind(ticket: Ticket, ticketQuantity: Int, donationAmount: Float, eventCurrency: String?) {
        itemView.ticketName.text = ticket.name

        when (ticket.type) {
            TICKET_TYPE_DONATION -> {
                itemView.price.text = resource.getString(R.string.donation)
                itemView.subTotal.text = "$eventCurrency${"%2.f".format(donationAmount)}"
            }
            TICKET_TYPE_FREE -> {
                itemView.price.text = resource.getString(R.string.free)
                itemView.subTotal.text = "${eventCurrency}0.00"
            }
            TICKET_TYPE_PAID -> {
                itemView.price.text = "$eventCurrency${"%.2f".format(ticket.price)}"
                val subTotal: Float? = ticket.price.times(ticketQuantity)
                itemView.subTotal.text = "$eventCurrency${"%.2f".format(subTotal)}"
            }
        }

        itemView.qty.text = ticketQuantity.toString()
    }
}
