package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R

class TicketsRecyclerAdapter : RecyclerView.Adapter<TicketViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var selectedListener: TicketSelectedListener? = null
    private lateinit var ticketAndQuantity: List<Pair<Int, Int>>

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
        holder.bind(event, selectedListener, eventCurrency,
            if ((this::ticketAndQuantity.isInitialized)) ticketAndQuantity.get(position).second else 0)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    fun setTicketAndQty(ticketAndQty: List<Pair<Int, Int>>) {
        ticketAndQuantity = ticketAndQty
    }
}

interface TicketSelectedListener {
    fun onSelected(ticketId: Int, quantity: Int)
}
