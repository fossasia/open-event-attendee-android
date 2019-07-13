package org.fossasia.openevent.general.order

import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.databinding.ItemCardOrderBinding

class OrdersViewHolder(private val binding: ItemCardOrderBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        eventAndOrder: Pair<Event, Order>,
        showExpired: Boolean,
        listener: OrdersPagedListAdapter.OrderClickListener?
    ) {
        val event = eventAndOrder.first
        val order = eventAndOrder.second
        val formattedDateTime = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
        val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)

        with(binding) {
            this.event = event
            this.order = order
            eventTime = "Starts at $formattedTime $timezone"
            eventDate = formattedDateTime.dayOfMonth.toString()
            eventMonth = formattedDateTime.month.name.slice(0 until 3)
            expiredTicket = showExpired
            executePendingBindings()
        }

        itemView.setOnClickListener {
            listener?.onClick(event.id, order.identifier ?: "", order.id)
        }
    }
}
