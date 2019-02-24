package org.fossasia.openevent.general.order

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card_order_details.view.calendar
import kotlinx.android.synthetic.main.item_card_order_details.view.date
import kotlinx.android.synthetic.main.item_card_order_details.view.eventDetails
import kotlinx.android.synthetic.main.item_card_order_details.view.eventName
import kotlinx.android.synthetic.main.item_card_order_details.view.eventSummary
import kotlinx.android.synthetic.main.item_card_order_details.view.location
import kotlinx.android.synthetic.main.item_card_order_details.view.map
import kotlinx.android.synthetic.main.item_card_order_details.view.name
import kotlinx.android.synthetic.main.item_card_order_details.view.orderIdentifier
import kotlinx.android.synthetic.main.item_card_order_details.view.organizer
import kotlinx.android.synthetic.main.item_card_order_details.view.qrCodeView
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.EventUtils.loadMapUrl
import org.fossasia.openevent.general.utils.stripHtml

class OrderDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val qrCode = QrCode()

    fun bind(
        attendee: Attendee,
        event: Event?,
        orderIdentifier: String?,
        eventDetailsListener: OrderDetailsRecyclerAdapter.EventDetailsListener?
    ) {
        if (event != null) {
            val formattedDateTime = EventUtils.getLocalizedDateTime(event.startsAt)
            val formattedDate = EventUtils.getFormattedDateShort(formattedDateTime)
            val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
            val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)

            itemView.eventName.text = event.name
            itemView.location.text = event.locationName
            itemView.date.text = "$formattedDate\n$formattedTime $timezone"
            itemView.eventSummary.text = event.description?.stripHtml()
            itemView.organizer.text = event.organizerName

            itemView.map.setOnClickListener {
                val mapUrl = loadMapUrl(event)
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                val packageManager = itemView.context?.packageManager
                if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
                    itemView.context.startActivity(mapIntent)
                }
            }

            itemView.calendar.setOnClickListener {
                val intent = Intent(Intent.ACTION_INSERT)
                intent.type = "vnd.android.cursor.item/event"
                intent.putExtra(CalendarContract.Events.TITLE, event.name)
                intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
                intent.putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME, EventUtils.getTimeInMilliSeconds(event.startsAt))
                intent.putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME, EventUtils.getTimeInMilliSeconds(event.endsAt))
                itemView.context.startActivity(intent)
            }

            itemView.eventDetails.setOnClickListener {
                eventDetailsListener?.onClick(event.id)
            }
        }

        itemView.name.text = "${attendee.firstname} ${attendee.lastname}"
        itemView.orderIdentifier.text = orderIdentifier

        val bitmap = qrCode.generateQrBitmap(orderIdentifier, 200, 200)
        if (bitmap != null) {
            itemView.qrCodeView.setImageBitmap(bitmap)
        } else {
            itemView.qrCodeView.visibility = View.GONE
        }
    }
}
