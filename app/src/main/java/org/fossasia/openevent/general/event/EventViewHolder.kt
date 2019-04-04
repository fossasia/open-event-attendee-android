package org.fossasia.openevent.general.event

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_card_events.view.date
import kotlinx.android.synthetic.main.item_card_events.view.eventImage
import kotlinx.android.synthetic.main.item_card_events.view.eventName
import kotlinx.android.synthetic.main.item_card_events.view.favoriteFab
import kotlinx.android.synthetic.main.item_card_events.view.locationName
import kotlinx.android.synthetic.main.item_card_events.view.month
import kotlinx.android.synthetic.main.item_card_events.view.shareFab
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener
import org.fossasia.openevent.general.favorite.FAVORITE_EVENT_DATE_FORMAT
import timber.log.Timber

/**
 * The [RecyclerView.ViewHolder] class for Event items list in [EventsFragment]
 * It implements the LayoutContainer interface from Kotlin Android Extensions to cache view lookup calls.
 *
 * @param containerView The root view of this ViewHolder
 * @property eventClickListener The callback to be invoked when an event is clicked
 * @property favFabClickListener The callback to be invoked when the favorite FAB is clicked
 * @property shareFabClickListener The callback to be invoked when the share FAB is clicked
 */
class EventViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    var eventClickListener: EventClickListener? = null
    var favFabClickListener: FavoriteFabClickListener? = null
    var shareFabClickListener: ShareFabClickListener? = null

    /**
     * The function to bind the given data on the items in this recycler view.
     *
     * @param event The [Event] object whose details are to be bound
     * @param dateFormat The format in which to display the date on the events card
     */
    fun bind(
        event: Event,
        dateFormat: String
    ) {
        containerView.eventImage.setImageResource(R.drawable.header)
        containerView.eventName.text = event.name
        containerView.locationName.text = event.locationName

        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        if (dateFormat == FAVORITE_EVENT_DATE_FORMAT) {
            itemView.date.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        } else {
            itemView.date.text = startsAt.dayOfMonth.toString()
            itemView.month.text = startsAt.month.name.slice(0 until 3)
        }

        setFabBackground(event.favorite)

        event.originalImageUrl?.let { url ->
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.header)
                .into(containerView.eventImage)
        }

        containerView.setOnClickListener {
            eventClickListener?.onClick(event.id)
                ?: Timber.e("Event Click listener on ${this::class.java.canonicalName} is null")
        }

        containerView.shareFab.setOnClickListener {
            shareFabClickListener?.onClick(event)
                ?: Timber.e("Share Fab Click listener on ${this::class.java.canonicalName} is null")
        }

        containerView.favoriteFab.setOnClickListener {
            favFabClickListener?.onClick(event, adapterPosition)
                ?: Timber.e("Favorite Fab Click listener on ${this::class.java.canonicalName} is null")
        }
    }

    private fun setFabBackground(isFavorite: Boolean) {
        if (isFavorite) {
            containerView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite)
        } else {
            containerView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite_border)
        }
    }
}
