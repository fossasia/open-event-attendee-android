package org.fossasia.openevent.general.ticket

import android.R
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_ticket.view.*

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(ticket: Ticket, selectedListener: TicketSelectedListener?) {
        itemView.ticketName.text = ticket.name

        if (!ticket.minOrder.isNullOrEmpty() && !ticket.maxOrder.isNullOrEmpty()) {
            val spinnerList = ArrayList<String>()
            for (i in ticket.minOrder!!.toInt()..ticket.maxOrder!!.toInt()) {
                spinnerList.add(Integer.toString(i))
            }
            itemView.orderRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                    itemView.order.text = spinnerList[pos]
                    selectedListener?.onSelected(ticket.id, spinnerList[pos].toInt())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
            itemView.orderRange.adapter = ArrayAdapter(itemView.context, R.layout.select_dialog_singlechoice, spinnerList)
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