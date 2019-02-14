package org.fossasia.openevent.general.event

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_events.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.favorite.FAVORITE_EVENT_DATE_FORMAT

class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        event: Event,
        clickListener: RecyclerViewClickListener?,
        favoriteListener: FavoriteFabListener?,
        dateFormat: String
    ) {
        itemView.eventName.text = event.name
        itemView.locationName.text = event.locationName

        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)

        if (dateFormat == FAVORITE_EVENT_DATE_FORMAT) {
            itemView.date.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        } else {
            itemView.date.text = startsAt.dayOfMonth.toString()
            itemView.month.text = startsAt.month.name.slice(0 until 3)
        }

        setFabBackground(event.favorite)
        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(itemView.eventImage)
        }

        itemView.setOnClickListener {
            clickListener?.onClick(event.id)
        }

        itemView.shareFab.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND

            sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
            sendIntent.type = "text/plain"
            itemView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
        }

        itemView.favoriteFab.setOnClickListener {
            favoriteListener?.onClick(event, event.favorite)
        }
    }

    fun setFabBackground(isFavorite: Boolean) {
        if (isFavorite) {
            itemView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite)
        } else {
            itemView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite_border)
        }
    }
}
