package org.fossasia.openevent.general.ticket

import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.item_ticket.view.description
import kotlinx.android.synthetic.main.item_ticket.view.discountPrice
import kotlinx.android.synthetic.main.item_ticket.view.donationInput
import kotlinx.android.synthetic.main.item_ticket.view.moreInfoSection
import kotlinx.android.synthetic.main.item_ticket.view.order
import kotlinx.android.synthetic.main.item_ticket.view.orderQtySection
import kotlinx.android.synthetic.main.item_ticket.view.orderRange
import kotlinx.android.synthetic.main.item_ticket.view.price
import kotlinx.android.synthetic.main.item_ticket.view.priceInfo
import kotlinx.android.synthetic.main.item_ticket.view.priceSection
import kotlinx.android.synthetic.main.item_ticket.view.saleInfo
import kotlinx.android.synthetic.main.item_ticket.view.seeMoreInfoText
import kotlinx.android.synthetic.main.item_ticket.view.taxInfo
import kotlinx.android.synthetic.main.item_ticket.view.ticketDateText
import kotlinx.android.synthetic.main.item_ticket.view.ticketName
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.discount.DiscountCode
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate
import org.fossasia.openevent.general.event.tax.Tax
import org.threeten.bp.DateTimeUtils

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
        eventTimeZone: String?,
        ticketQuantity: Int,
        donationAmount: Float,
        discountCode: DiscountCode? = null,
        tax: Tax?
    ) {
        itemView.ticketName.text = ticket.name
        setupTicketSaleDate(ticket, eventTimeZone)

        setMoreInfoText()
        itemView.seeMoreInfoText.setOnClickListener {
            itemView.moreInfoSection.isVisible = !itemView.moreInfoSection.isVisible
            setMoreInfoText()
        }

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

        var ticketPrice = ticket.price
        if (tax?.rate != null) {
            if (!tax.isTaxIncludedInPrice) {
                val taxPrice = (ticketPrice * tax.rate / 100)
                ticketPrice += taxPrice
                itemView.taxInfo.text = "(+ $eventCurrency${"%.2f".format(taxPrice)} ${tax.name})"
            } else {
                val taxPrice = (ticket.price * tax.rate) / (100 + tax.rate)
                itemView.taxInfo.text = "( $eventCurrency${"%.2f".format(taxPrice)} ${tax.name} included)"
            }
        }

        when (ticket.type) {
            TICKET_TYPE_DONATION -> {
                itemView.price.text = resource.getString(R.string.donation)
                itemView.priceSection.isVisible = false
                itemView.donationInput.isVisible = true
                if (donationAmount > 0F) itemView.donationInput.setText(donationAmount.toString())
                setupDonationTicketPicker()
            }
            TICKET_TYPE_FREE -> {
                itemView.price.text = resource.getString(R.string.free)
                itemView.priceSection.isVisible = true
                itemView.donationInput.isVisible = false
            }
            TICKET_TYPE_PAID -> {
                itemView.price.text = "$eventCurrency${"%.2f".format(ticketPrice)}"
                itemView.priceSection.isVisible = true
                itemView.donationInput.isVisible = false
            }
        }
        setupQtyPicker(minQty, maxQty, selectedListener, ticket, ticketQuantity, ticket.type)

        val price = if (tax?.rate != null && tax.isTaxIncludedInPrice) (ticket.price * 100) / (100 + tax.rate)
        else ticket.price
        val priceDetail = if (price > 0) "$eventCurrency${"%.2f".format(price)}"
        else resource.getString(R.string.free)
        val priceInfo = "<b>${resource.getString(R.string.price)}:</b> $priceDetail"
        itemView.priceInfo.text = Html.fromHtml(priceInfo)

        if (ticket.description.isNullOrEmpty()) {
            itemView.description.isVisible = false
        } else {
            itemView.description.isVisible = true
            itemView.description.text = ticket.description
        }

        if (discountCode?.value != null && ticket.price != 0.toFloat()) {
            itemView.discountPrice.visibility = View.VISIBLE
            itemView.price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            itemView.discountPrice.text =
                if (discountCode.type == AMOUNT) "$eventCurrency${ticketPrice - discountCode.value}"
                else "$eventCurrency${"%.2f".format(ticketPrice - (ticketPrice * discountCode.value / 100))}"
        }
    }

    private fun setupDonationTicketPicker() {
        itemView.donationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do Nothing*/ }
            override fun afterTextChanged(s: Editable?) {
                val donationEntered = s.toString()
                if (donationEntered.isNotBlank() && donationEntered.toFloat() > 0) {
                    if (itemView.orderRange.selectedItemPosition == 0)
                        itemView.orderRange.setSelection(1)
                } else {
                    itemView.orderRange.setSelection(0)
                }
            }
        })
    }

    private fun setupQtyPicker(
        minQty: Int,
        maxQty: Int,
        selectedListener: TicketSelectedListener?,
        ticket: Ticket,
        ticketQuantity: Int,
        ticketType: String?
    ) {
        if (minQty > 0 && maxQty > 0) {
            val spinnerList = ArrayList<String>()
            spinnerList.add("0")
            for (i in minQty..maxQty) {
                spinnerList.add(i.toString())
            }
            itemView.orderRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                    val donationEntered = itemView.donationInput.text.toString()
                    val donation = if (donationEntered.isEmpty()) 0F else donationEntered.toFloat()
                    itemView.order.text = spinnerList[pos]
                    selectedListener?.onSelected(ticket.id, spinnerList[pos].toInt(), donation)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            val arrayAdapter = object : ArrayAdapter<String>(itemView.context,
                android.R.layout.select_dialog_singlechoice, spinnerList) {
                override fun isEnabled(position: Int): Boolean {
                    if (TICKET_TYPE_DONATION == ticketType) {
                        val donationEntered = itemView.donationInput.text.toString()
                        val donation = if (donationEntered.isEmpty()) 0F else donationEntered.toFloat()
                        return if (donation > 0F)
                            position != 0
                        else
                            position == 0
                    }
                    return super.isEnabled(position)
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    if (TICKET_TYPE_DONATION == ticketType) {
                        if (view is TextView) {
                            val donationEntered = itemView.donationInput.text.toString()
                            val donation = if (donationEntered.isEmpty()) 0F else donationEntered.toFloat()
                            if (donation > 0F)
                                view.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                            else
                                view.setTextColor(if (position == 0) Color.BLACK else Color.GRAY)
                        }
                    }
                    return view
                }
            }
            itemView.orderRange.adapter = arrayAdapter
            val currentQuantityPosition = spinnerList.indexOf(ticketQuantity.toString())
            if (currentQuantityPosition != -1) {
                itemView.orderRange.setSelection(currentQuantityPosition)
                itemView.order.text = ticketQuantity.toString()
            }
        }
    }

    private fun setMoreInfoText() {
        itemView.seeMoreInfoText.text = resource.getString(
            if (itemView.moreInfoSection.isVisible) R.string.see_less else R.string.see_more)
    }

    private fun setupTicketSaleDate(ticket: Ticket, timeZone: String?) {
        val startAt = ticket.salesStartsAt
        val endAt = ticket.salesEndsAt
        if (startAt != null && endAt != null && timeZone != null) {
            val startsAt = EventUtils.getEventDateTime(startAt, timeZone)
            val endsAt = EventUtils.getEventDateTime(endAt, timeZone)
            val startDate = DateTimeUtils.toDate(startsAt.toInstant())
            val endDate = DateTimeUtils.toDate(endsAt.toInstant())
            val currentDate = Date()

            if (currentDate < startDate) {
                itemView.ticketDateText.isVisible = true
                itemView.ticketDateText.text = resource.getString(R.string.not_open)
                itemView.orderQtySection.isVisible = false
            } else if (startDate < currentDate && currentDate < endDate) {
                itemView.ticketDateText.isVisible = false
                itemView.orderQtySection.isVisible = true
            } else {
                itemView.ticketDateText.text = resource.getString(R.string.ended)
                itemView.ticketDateText.isVisible = true
                itemView.orderQtySection.isVisible = false
            }

            val salesEnd = "<b>${resource.getString(R.string.sales_end)}</b> ${getFormattedDate(endsAt)}"
            itemView.saleInfo.text = Html.fromHtml(salesEnd)
        }
    }
}
