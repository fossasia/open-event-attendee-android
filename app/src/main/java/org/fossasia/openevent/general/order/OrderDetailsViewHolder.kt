package org.fossasia.openevent.general.order

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_card_order_details.view.*
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils

class OrderDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(attendee: Attendee, event: Event?, orderIdentifier: String?) {
        val formattedDateTime = EventUtils.getLocalizedDateTime(event?.startsAt.toString())
        val formattedDate = EventUtils.getFormattedDateShort(formattedDateTime)
        val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
        val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)

        itemView.name.text = "${attendee.firstname} ${attendee.lastname}"
        itemView.eventName.text = event?.name
        itemView.date.text = "$formattedDate\n$formattedTime $timezone"
        itemView.location.text = event?.locationName
        itemView.orderIdentifier.text = orderIdentifier
        itemView.eventSummary.text = event?.description
        itemView.organizer.text = event?.organizerName
    }
}