package org.fossasia.openevent.general.ticket

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.discount.DiscountCode

class TicketsRecyclerAdapter : RecyclerView.Adapter<TicketViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var discountCode: DiscountCode? = null
    private var selectedListener: TicketSelectedListener? = null
    private lateinit var ticketAndQuantity: List<Triple<Int, Int, Float>>

    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
    }

    fun setSelectListener(listener: TicketSelectedListener?) {
        selectedListener = listener
    }

    fun setCurrency(currencyCode: String?) {
        eventCurrency = currencyCode
    }

    fun applyDiscount(discountCode: DiscountCode) {
        this.discountCode = discountCode
    }

    fun cancelDiscountCode() {
        this.discountCode = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        var currentDiscountCode: DiscountCode? = null
        discountCode?.tickets?.forEach {
            if (it.id.toInt() == ticket.id)
                currentDiscountCode = discountCode
        }
        val qty = if ((this::ticketAndQuantity.isInitialized)) ticketAndQuantity[position].second else 0
            holder.bind(ticket, selectedListener, eventCurrency, qty, currentDiscountCode)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    fun setTicketAndQty(ticketAndQty: List<Triple<Int, Int, Float>>) {
        ticketAndQuantity = ticketAndQty
    }
}

interface TicketSelectedListener {
    fun onSelected(ticketId: Int, quantity: Int)
    fun onDonationSelected(ticketId: Int, donation: Float)
}
