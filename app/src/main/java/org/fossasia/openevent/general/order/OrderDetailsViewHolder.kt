package org.fossasia.openevent.general.order

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card_order_details.view.calendar
import kotlinx.android.synthetic.main.item_card_order_details.view.eventDetails
import kotlinx.android.synthetic.main.item_card_order_details.view.map
import kotlinx.android.synthetic.main.item_card_order_details.view.mainLayout
import kotlinx.android.synthetic.main.item_card_order_details.view.qrCodeView
import kotlinx.android.synthetic.main.item_card_order_details.view.downloadButton
import kotlinx.android.synthetic.main.item_card_order_details.view.checkedInLayout
import kotlinx.android.synthetic.main.item_card_order_details.view.notCheckedInLayout
import kotlinx.android.synthetic.main.item_card_order_details.view.notAvailableTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.databinding.ItemCardOrderDetailsBinding
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.EventUtils.loadMapUrl
import org.fossasia.openevent.general.utils.stripHtml
import org.jetbrains.anko.browse
import java.lang.StringBuilder

class OrderDetailsViewHolder(private val binding: ItemCardOrderDetailsBinding) : RecyclerView.ViewHolder(binding.root) {
    private val qrCode = QrCode()

    fun bind(
        attendee: Attendee,
        event: Event?,
        orderIdentifier: String?,
        eventDetailsListener: OrderDetailsRecyclerAdapter.EventDetailsListener?,
        qrImageClickListener: OrderDetailsRecyclerAdapter.QrImageClickListener?,
        count: Int,
        position: Int
    ) {
        if (event == null) return
        val formattedDateTime = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val formattedDate = EventUtils.getFormattedDateShort(formattedDateTime)
        val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
        val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)
        val resources = itemView.resources
        val ticketIdentifier = "$orderIdentifier-${attendee.id}"

        with(binding) {
            this.event = event
            this.attendee = attendee
            this.count = count
            this.position = position + 1
            eventDate = "$formattedDate\n$formattedTime $timezone"
            identifier = ticketIdentifier
        }

        if (position == 0) {
            val params: FrameLayout.LayoutParams =
                FrameLayout.LayoutParams(resources.getDimension(R.dimen.ticket_width).toInt(), MATCH_PARENT)
            params.leftMargin = resources.getDimension(R.dimen.layout_margin_large).toInt()
            itemView.mainLayout.layoutParams = params
        } else if (position + 1 == count) {
            val params: FrameLayout.LayoutParams =
                FrameLayout.LayoutParams(resources.getDimension(R.dimen.ticket_width).toInt(), MATCH_PARENT)
            params.rightMargin = resources.getDimension(R.dimen.layout_margin_large).toInt()
            itemView.mainLayout.layoutParams = params
        }

        if (attendee.isCheckedIn != null) {
            itemView.checkedInLayout.isVisible = attendee.isCheckedIn
            itemView.notCheckedInLayout.isVisible = !attendee.isCheckedIn
        } else {
            itemView.notAvailableTextView.visibility = View.VISIBLE
        }

        itemView.checkedInLayout.setOnClickListener {
            if (attendee.checkinTimes.isNullOrEmpty()) return@setOnClickListener
            val checkInTime = attendee.checkinTimes.split(",").toTypedArray()
            val times = StringBuilder()
            for (i in checkInTime.indices) {
                val dateTime = EventUtils.getEventDateTime(checkInTime[i], null)
                times.append(EventUtils.getFormattedDate(dateTime) + "\n")
                times.append(EventUtils.getFormattedTime(dateTime) + "\n\n")
            }
            if (attendee.isCheckedOut) times.append(resources.getString(R.string.checked_out))
            AlertDialog.Builder(itemView.context)
                .setTitle(resources.getString(R.string.check_in_times))
                .setMessage(resources.getString(R.string.check_in_times))
                .setMessage(times)
                .setCancelable(true)
                .show()
        }

        itemView.map.setOnClickListener {
            val mapUrl = loadMapUrl(event)
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            val packageManager = itemView.context?.packageManager
            if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
                itemView.context.startActivity(mapIntent)
            }
        }
        if (!attendee.pdfUrl.isNullOrBlank()) {
            itemView.downloadButton.isEnabled = true
            itemView.downloadButton.setOnClickListener {
                itemView.context.browse(attendee.pdfUrl)
            }
        }

        itemView.calendar.setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra(CalendarContract.Events.TITLE, event.name)
            intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.locationName)
            intent.putExtra(CalendarContract.Events.CALENDAR_TIME_ZONE, event.timezone)
            intent.putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                EventUtils.getTimeInMilliSeconds(event.startsAt, event.timezone))
            intent.putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                EventUtils.getTimeInMilliSeconds(event.endsAt, event.timezone))
            itemView.context.startActivity(intent)
        }

        itemView.eventDetails.setOnClickListener {
            eventDetailsListener?.onClick(event.id)
        }

        val bitmap = qrCode.generateQrBitmap(ticketIdentifier, 400, 400)

        if (bitmap != null) {
            itemView.qrCodeView.setImageBitmap(bitmap)
            itemView.qrCodeView.setOnClickListener {
                qrImageClickListener?.onClick(bitmap)
            }
        } else {
            itemView.qrCodeView.visibility = View.GONE
        }
    }
}
