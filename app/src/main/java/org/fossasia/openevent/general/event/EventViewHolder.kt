package org.fossasia.openevent.general.event

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_card_events.view.chipTags
import kotlinx.android.synthetic.main.item_card_events.view.eventImage
import kotlinx.android.synthetic.main.item_card_events.view.favoriteFab
import kotlinx.android.synthetic.main.item_card_events.view.shareFab
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.databinding.ItemCardEventsBinding

/**
 * The [RecyclerView.ViewHolder] class for Event items list in [EventsFragment]
 * It implements the LayoutContainer interface from Kotlin Android Extensions to cache view lookup calls.
 *
 * @param containerView The root view of this ViewHolder
 * @property eventClickListener The callback to be invoked when an event is clicked
 * @property favFabClickListener The callback to be invoked when the favorite FAB is clicked
 * @property shareFabClickListener The callback to be invoked when the share FAB is clicked
 */
class EventViewHolder(private val binding: ItemCardEventsBinding) : RecyclerView.ViewHolder(binding.root) {
    var eventClickListener: EventClickListener? = null
    var favFabClickListener: FavoriteFabClickListener? = null
    var hashTagClickListAdapter: EventHashTagClickListener? = null

    fun bind(
        event: Event,
        itemPosition: Int
    ) {
        val time = EventUtils.getEventDateTime(event.startsAt, event.timezone)

        with(binding) {
            this.event = event
            this.position = position
            position = itemPosition
            monthTime = time.month.name.slice(0 until 3)
            dateTime = time.dayOfMonth.toString()
            executePendingBindings()
        }

        itemView.shareFab.scaleType = ImageView.ScaleType.CENTER
        itemView.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        if (itemView.chipTags != null) {
            itemView.chipTags.removeAllViews()
            event.eventType?.let {
                addChips(it.name)
            }
            event.eventTopic?.let {
                addChips(it.name)
            }
            event.eventSubTopic?.let {
                addChips(it.name)
            }
        }

        itemView.setOnClickListener {
            eventClickListener?.onClick(event.id, itemView.eventImage)
        }

        itemView.shareFab.setOnClickListener {
            EventUtils.share(event, itemView.context)
        }

        itemView.favoriteFab.setOnClickListener {
            favFabClickListener?.onClick(event, adapterPosition)
        }
    }

    private fun addChips(name: String) {
        val chip = Chip(itemView.context)
        chip.text = name
        chip.isCheckable = false
        chip.chipStartPadding = 0f
        chip.chipEndPadding = 0f
        chip.chipStrokeWidth = 2f
        chip.chipStrokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
        chip.setOnClickListener {
            hashTagClickListAdapter?.onClick(name)
        }
        itemView.chipTags.addView(chip)
    }
}
