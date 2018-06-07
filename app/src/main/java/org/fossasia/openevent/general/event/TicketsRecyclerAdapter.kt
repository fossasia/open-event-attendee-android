package org.fossasia.openevent.general.event

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*

class TicketsRecyclerAdapter : RecyclerView.Adapter<TicketViewHolder>() {

    private val tickets = ArrayList<Ticket>()

    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val event = tickets[position]

        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

}