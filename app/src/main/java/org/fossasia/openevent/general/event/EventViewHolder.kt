package org.fossasia.openevent.general.event

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_events.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.favorite.FAVORITE_EVENT_DATE_FORMAT

class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var eventImage: ImageView = itemView.findViewById(ItemCardEventsUi.eventImage)
    var date: TextView = itemView.findViewById(ItemCardEventsUi.date)
    var month: TextView = itemView.findViewById(ItemCardEventsUi.month)
    var locationName: TextView = itemView.findViewById(ItemCardEventsUi.locationName)
    var eventName: TextView = itemView.findViewById(ItemCardEventsUi.eventName)
    var shareFab: FloatingActionButton = itemView.findViewById(ItemCardEventsUi.shareFab)
    var favoriteFab: FloatingActionButton = itemView.findViewById(ItemCardEventsUi.favFab)

    fun bind(
        event: Event,
        clickListener: RecyclerViewClickListener?,
        favoriteListener: FavoriteFabListener?,
        dateFormat: String
    ) {
        eventName.text = event.name
        locationName.text = event.locationName

        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        if (dateFormat == FAVORITE_EVENT_DATE_FORMAT) {
            date.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        } else {
            date.text = startsAt.dayOfMonth.toString()
            month.text = startsAt.month.name.slice(0 until 3)
        }

        setFabBackground(event.favorite)
        event.originalImageUrl?.let {
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.ic_launcher_background)
                .into(eventImage)
        }

        itemView.setOnClickListener {
            clickListener?.onClick(event.id)
        }

        shareFab.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND

            sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
            sendIntent.type = "text/plain"
            itemView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
        }

        favoriteFab.setOnClickListener {
            favoriteListener?.onClick(event, event.favorite)
        }
    }

    fun setFabBackground(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteFab.setImageResource(R.drawable.ic_baseline_favorite)
        } else {
            favoriteFab.setImageResource(R.drawable.ic_baseline_favorite_border)
        }
    }
}
