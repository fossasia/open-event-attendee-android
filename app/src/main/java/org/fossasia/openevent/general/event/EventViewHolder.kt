package org.fossasia.openevent.general.event

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_events.view.*
import org.fossasia.openevent.general.R



class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(event: Event, clickListener: RecyclerViewClickListener?) {
        itemView.eventName.text = event.name
        itemView.description.text = event.description

        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)

        itemView.date.text = startsAt.dayOfMonth.toString()
        itemView.month.text = startsAt.month.name.slice(0 until 3)
        itemView.year.text = startsAt.year.toString()

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(itemView.eventImage)
        }

        itemView.setOnClickListener{
            clickListener?.onClick(event.id)
        }

        itemView.shareFab.setOnClickListener{
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND

            sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
            sendIntent.type = "text/plain"
            itemView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
        }
    }
}