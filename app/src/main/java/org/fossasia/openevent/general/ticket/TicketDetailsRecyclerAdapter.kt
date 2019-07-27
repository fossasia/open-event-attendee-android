package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R

class TicketDetailsRecyclerAdapter : RecyclerView.Adapter<TicketDetailsViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var qty = ArrayList<Int>()
    private var donationsList = ArrayList<Float>()

    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
        notifyDataSetChanged()
    }

    fun setCurrency(currencyCode: String?) {
        eventCurrency = currencyCode
    }

    fun setDonations(donations: List<Float>) {
        if (donationsList.isNotEmpty()) donationsList.clear()
        donationsList.addAll(donations)
        notifyDataSetChanged()
    }

    fun setQuantity(ticketQuantities: List<Int>) {
        if (qty.isNotEmpty())qty.clear()
        qty.addAll(ticketQuantities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket_details, parent, false)
        return TicketDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketDetailsViewHolder, position: Int) {
        holder.bind(tickets[position], qty[position], donationsList[position], eventCurrency)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }
}
