package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R

class TicketsRecyclerAdapter : RecyclerView.Adapter<TicketViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var selectedListener: TicketSelectedListener? = null
    var ticketIdAndQty = HashMap<Int , Int>()


    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
    }

    fun setSelectListener(listener: TicketSelectedListener) {
        selectedListener = listener
    }

    fun setCurrency(currencyCode: String?) {
        eventCurrency = currencyCode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val event = tickets[position]
        holder.bind(event, selectedListener, eventCurrency, ticketIdAndQty[event.id])
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    fun setSelectedTickets(ticketIdAndQty: HashMap<Int, Int>) {
        this.ticketIdAndQty=ticketIdAndQty
    }
}

interface TicketSelectedListener {
    fun onSelected(ticketId: Int, quantity: Int)
}
