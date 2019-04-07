package org.fossasia.openevent.general.order

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_order.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils

class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        event: Event,
        clickListener: OrdersRecyclerAdapter.OrderClickListener?,
        orderIdentifier: String?,
        attendeesNumber: Int,
        showExpired: Boolean
    ) {
        val formattedDateTime = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
        val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)

        itemView.eventName.text = event.name
        itemView.time.text = "Starts at $formattedTime $timezone"
        itemView.setOnClickListener {
            orderIdentifier?.let { it1 -> clickListener?.onClick(event.id, it1) }
        }

        if (attendeesNumber == 1) {
            itemView.ticketsNumber.text = "See $attendeesNumber Ticket"
        } else {
            itemView.ticketsNumber.text = "See $attendeesNumber Tickets"
        }

        itemView.date.text = formattedDateTime.dayOfMonth.toString()
        itemView.month.text = formattedDateTime.month.name.slice(0 until 3)

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.header)
                    .into(itemView.eventImage)
        }
        if (!showExpired) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0F)
            itemView.eventImage.colorFilter = ColorMatrixColorFilter(matrix)
        }
    }
}
