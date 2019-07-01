package org.fossasia.openevent.general.ticket

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_ticket.view.order
import kotlinx.android.synthetic.main.item_ticket.view.orderRange
import kotlinx.android.synthetic.main.item_ticket.view.price
import kotlinx.android.synthetic.main.item_ticket.view.ticketName
import kotlinx.android.synthetic.main.item_ticket.view.discountPrice
import kotlinx.android.synthetic.main.item_ticket.view.donationInput
import kotlinx.android.synthetic.main.item_ticket.view.orderQtySection
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.discount.DiscountCode

const val AMOUNT = "amount"
const val TICKET_TYPE_FREE = "free"
const val TICKET_TYPE_PAID = "paid"
const val TICKET_TYPE_DONATION = "donation"

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val resource = Resource()

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

        itemView.order.setOnClickListener {
            itemView.orderRange.performClick()
        }

        when (ticket.type) {
            TICKET_TYPE_DONATION -> {
                itemView.price.text = resource.getString(R.string.enter_donation)
                itemView.orderQtySection.isVisible = false
                itemView.donationInput.isVisible = true
                setupDonationTicketPicker(selectedListener, ticket)
            }
            TICKET_TYPE_FREE -> {
                itemView.price.text = resource.getString(R.string.free)
                itemView.orderQtySection.isVisible = true
                itemView.donationInput.isVisible = false
                setupQtyPicker(minQty, maxQty, selectedListener, ticket, ticketQuantity)
            }
            TICKET_TYPE_PAID -> {
                itemView.price.text = "$eventCurrency${ticket.price}"
                itemView.orderQtySection.isVisible = true
                itemView.donationInput.isVisible = false
                setupQtyPicker(minQty, maxQty, selectedListener, ticket, ticketQuantity)
            }
        }

        if (discountCode?.value != null && ticket.price != null && ticket.price != 0.toFloat()) {
            itemView.discountPrice.visibility = View.VISIBLE
            itemView.price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            itemView.discountPrice.text =
                if (discountCode.type == AMOUNT) "$eventCurrency${ticket.price - discountCode.value}"
                else "$eventCurrency${ticket.price - (ticket.price * discountCode.value / 100)}"
        }
    }

    private fun setupDonationTicketPicker(
        selectedListener: TicketSelectedListener?,
        ticket: Ticket
    ) {
        itemView.donationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do Nothing*/ }
            override fun afterTextChanged(s: Editable?) {
                val donationEntered = s.toString()
                if (donationEntered.isNotBlank()) {
                    val donation = donationEntered.toFloat()
                    if (donation > 0F) {
                        selectedListener?.onDonationSelected(ticket.id, donation)
                    }
                }
            }
        })
    }

    private fun setupQtyPicker(
        minQty: Int,
        maxQty: Int,
        selectedListener: TicketSelectedListener?,
        ticket: Ticket,
        ticketQuantity: Int
    ) {
        if (minQty > 0 && maxQty > 0) {
            val spinnerList = ArrayList<String>()
            spinnerList.add("0")
            for (i in minQty..maxQty) {
                spinnerList.add(i.toString())
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
    }
}
