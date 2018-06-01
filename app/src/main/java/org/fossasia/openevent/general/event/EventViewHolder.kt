package org.fossasia.openevent.general.event

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_events.view.*
import org.fossasia.openevent.general.R
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(event: Event) {
        itemView.eventName.text = event.name
        itemView.description.text = event.description

        val splitDay = dateFormat(event.startsAt).split(" ")

        itemView.date.text = splitDay[0]
        itemView.month.text = splitDay[1].toUpperCase()
        itemView.year.text = splitDay[2]

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(itemView.eventImage)
        }

        itemView.shareFab.setOnClickListener{
            val eventName = if(!(event.name).isEmpty()) event.name else ""
            val description = if (!(event.description).isNullOrEmpty()) event.description else ""
            val startDate = if(!dateFormat(event.startsAt).isEmpty()) dateFormat(event.startsAt) else ""
            val endDate = if(!dateFormat(event.endsAt).isEmpty()) dateFormat(event.endsAt) else ""
            val startTime = if(!timeFormat(event.startsAt).isEmpty()) timeFormat(event.startsAt) else ""
            val endTime = if(!timeFormat(event.endsAt).isEmpty()) timeFormat(event.endsAt) else ""
            val eventUrl = if (!(event.externalEventUrl).isNullOrEmpty()) Uri.parse(event.externalEventUrl) else ""

            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND

            val message = StringBuilder()
            if(!eventName.isEmpty()) message.append(itemView.resources.getString(R.string.event_name)).append(eventName).append("\n")
            if(!description.isNullOrEmpty()) message.append(itemView.resources.getString(R.string.event_description)).append(description).append("\n")
            if(!startDate.isEmpty()) message.append(itemView.resources.getString(R.string.starts_on)).append(startDate).append("\n")
            if(!startTime.isEmpty()) message.append(itemView.resources.getString(R.string.start_time)).append(startTime).append("\n")
            if(!endDate.isEmpty()) message.append(itemView.resources.getString(R.string.ends_on)).append(endDate).append("\n")
            if(!endTime.isEmpty()) message.append(itemView.resources.getString(R.string.end_time)).append(endTime).append("\n")
            if(!eventUrl.toString().isEmpty()) message.append(itemView.resources.getString(R.string.event_link)).append(eventUrl)

            sendIntent.putExtra(Intent.EXTRA_TEXT, message.toString())
            sendIntent.type = "text/plain"
            itemView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
        }
    }

    private fun timeFormat(dateString: String?): String {
        val string = dateString?.substring(0, 9)
        val format = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
        try {
            val date = format.parse(string)
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return dateFormat.format(date)
        } catch (e: ParseException) {
            Timber.e(e, "Time conversion failed %s", dateString)
        }

        return ""
    }

    private fun dateFormat(dateString: String?): String {
        val string = dateString?.substring(0, 9)
        val format = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
        try {
            val date = format.parse(string)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return dateFormat.format(date)
        } catch (e: ParseException) {
            Timber.e(e, "Date conversion failed %s", dateString)
        }

        return ""
    }
}