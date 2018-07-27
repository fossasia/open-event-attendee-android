package org.fossasia.openevent.general.ticket

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import kotlin.collections.ArrayList

class TicketDetailsRecyclerAdapter : RecyclerView.Adapter<TicketDetailsViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var qty = ArrayList<Int>()

    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
    }

    fun setCurrency(currencyCode: String?) {
        eventCurrency = currencyCode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket_details, parent, false)
        return TicketDetailsViewHolder(view)
    }

    fun setQty(qty: ArrayList<Int>) {
        this.qty = qty
    }

    override fun onBindViewHolder(holder: TicketDetailsViewHolder, position: Int) {
        val event = tickets[position]

        holder.bind(event, qty, eventCurrency)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

}