package org.fossasia.openevent.general.event

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_card_events.view.chipTags
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
import org.fossasia.openevent.general.favorite.FAVORITE_EVENT_DATE_FORMAT
import timber.log.Timber
import java.lang.Exception

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

        event.eventType.let {
            if (it != null)
                addchips(it.name)
        }
        event.eventTopic.let {
            if (it != null)
                addchips(it.name)
        }
        event.eventSubTopic.let {
            if (it != null)
                addchips(it.name)
        }

        event.originalImageUrl?.let { url ->
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.header)
                .into(containerView.eventImage, object : Callback {
                    override fun onSuccess() {
                        containerView.eventImage.tag = "image_loading_success"
                    }

                    override fun onError(e: Exception?) {
                        Timber.e(e)
                    }
                })
        }

        containerView.setOnClickListener {
            eventClickListener?.onClick(event.id)
                ?: Timber.e("Event Click listener on ${this::class.java.canonicalName} is null")
        }

        containerView.shareFab.setOnClickListener {
            EventUtils.share(event, itemView.eventImage)
        }

        containerView.favoriteFab.setOnClickListener {
            favFabClickListener?.onClick(event, adapterPosition)
                ?: Timber.e("Favorite Fab Click listener on ${this::class.java.canonicalName} is null")
        }
    }

    private fun addchips(name: String) {
        val chip = Chip(containerView.context)
        chip.text = name
        chip.isCheckable = false
        chip.chipStartPadding = 0f
        chip.chipEndPadding = 0f
        chip.chipStrokeWidth = 2f
        chip.chipStrokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(containerView.context, R.color.colorPrimary))
        containerView.chipTags.addView(chip)
    }

    private fun setFabBackground(isFavorite: Boolean) {
        if (isFavorite) {
            containerView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite)
        } else {
            containerView.favoriteFab.setImageResource(R.drawable.ic_baseline_favorite_border)
        }
    }
}
