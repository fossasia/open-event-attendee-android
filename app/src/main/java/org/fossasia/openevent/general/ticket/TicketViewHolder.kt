package org.fossasia.openevent.general.ticket

import android.graphics.Paint
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_ticket.view.order
import kotlinx.android.synthetic.main.item_ticket.view.orderRange
import kotlinx.android.synthetic.main.item_ticket.view.price
import kotlinx.android.synthetic.main.item_ticket.view.ticketName
import kotlinx.android.synthetic.main.item_ticket.view.discountPrice
import org.fossasia.openevent.general.discount.DiscountCode

const val AMOUNT = "amount"

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        ticket: Ticket,
        selectedListener: TicketSelectedListener?,
        eventCurrency: String?,
        ticketQuantity: Int,
        discountCode: DiscountCode? = null
    ) {
        itemView.ticketName.text = ticket.name
        var minQty = ticket.minOrder
        var maxQty = ticket.maxOrder
        if (discountCode?.minQuantity != null)
            minQty = discountCode.minQuantity
        if (discountCode?.maxQuantity != null)
            maxQty = discountCode.maxQuantity

        if (discountCode == null) {
            minQty = ticket.minOrder
            maxQty = ticket.maxOrder
            itemView.discountPrice.visibility = View.GONE
            itemView.price.paintFlags = 0
        }

        if (minQty > 0 && maxQty > 0) {
            val spinnerList = ArrayList<String>()
            spinnerList.add("0")
            for (i in minQty..maxQty) {
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
            itemView.orderRange.adapter = ArrayAdapter(itemView.context, android.R.layout.select_dialog_singlechoice,
                spinnerList)
            val currentQuantityPosition = spinnerList.indexOf(ticketQuantity.toString())
            if (currentQuantityPosition != -1) {
                itemView.orderRange.setSelection(currentQuantityPosition)
                itemView.order.text = ticketQuantity.toString()
            }
        }

        itemView.order.setOnClickListener {
            itemView.orderRange.performClick()
        }

        if (ticket.price != null) {
            itemView.price.text = "$eventCurrency${ticket.price}"
        }

        if (ticket.price == 0.toFloat()) {
            itemView.price.text = "Free"
        }

        if (discountCode?.value != null && ticket.price != null && ticket.price != 0.toFloat()) {
            itemView.discountPrice.visibility = View.VISIBLE
            itemView.price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            itemView.discountPrice.text =
                if (discountCode.type == AMOUNT) "$eventCurrency${ticket.price - discountCode.value}"
                else "$eventCurrency${ticket.price - (ticket.price * discountCode.value / 100)}"
        }
    }
}
