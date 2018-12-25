package org.fossasia.openevent.general.order

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_card_order_details.view.*
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils

class OrderDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val qrCode = QrCode()

    fun bind(
        attendee: Attendee,
        event: Event?,
        orderIdentifier: String?,
        eventDetailsListener: OrderDetailsRecyclerAdapter.EventDetailsListener?
    ) {
        val formattedDateTime = event?.startsAt?.let { EventUtils.getLocalizedDateTime(it) }
        val formattedDate = formattedDateTime?.let { EventUtils.getFormattedDateShort(it) }
        val formattedTime = formattedDateTime?.let { EventUtils.getFormattedTime(it) }
        val timezone = formattedDateTime?.let { EventUtils.getFormattedTimeZone(it) }

        itemView.name.text = "${attendee.firstname} ${attendee.lastname}"
        itemView.eventName.text = event?.name
        itemView.date.text = "$formattedDate\n$formattedTime $timezone"
        itemView.location.text = event?.locationName
        itemView.orderIdentifier.text = orderIdentifier
        itemView.eventSummary.text = event?.description
        itemView.organizer.text = event?.organizerName

        itemView.map.setOnClickListener {
            val mapUrl = event?.let { it1 -> loadMapUrl(it1) }
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            if (mapIntent.resolveActivity(itemView.context?.packageManager) != null) {
                itemView.context.startActivity(mapIntent)
            }
        }

        itemView.calendar.setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra(CalendarContract.Events.TITLE, event?.name)
            intent.putExtra(CalendarContract.Events.DESCRIPTION, event?.description)
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event?.startsAt?.let { it1 -> EventUtils.getTimeInMilliSeconds(it1) })
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event?.endsAt?.let { it1 -> EventUtils.getTimeInMilliSeconds(it1) })
            itemView.context.startActivity(intent)
        }

        itemView.eventDetails.setOnClickListener {
            event?.let { it1 -> eventDetailsListener?.onClick(it1.id) }
        }

        val bitmap = qrCode.generateQrBitmap(orderIdentifier, 200, 200)
        if (bitmap != null) {
            itemView.qrCodeView.setImageBitmap(bitmap)
        } else {
            itemView.qrCodeView.visibility = View.GONE
        }
    }

    fun loadMapUrl(event: Event): String {
        // load map url
        return "geo:<" + event.latitude + ">,<" + event.longitude + ">?q=<" + event.latitude + ">,<" + event.longitude + ">"
    }
}
