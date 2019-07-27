package org.fossasia.openevent.general.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.EventsDiffCallback
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.databinding.ItemCardFavoriteEventBinding
import org.fossasia.openevent.general.event.EventUtils.getEventDateTime
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate

/**
 * The RecyclerView adapter class for displaying favorite events list
 *
 * @param diffCallback The DiffUtil.ItemCallback implementation to be used with this adapter.
 * @property onEventClick The callback to be invoked when an event is clicked
 * @property onFavFabClick The callback to be invoked when the favorite FAB is clicked
 * @property onShareFabClick The callback to be invoked when the share FAB is clicked
 */
class FavoriteEventsListAdapter : ListAdapter<Event, FavoriteEventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val binding = ItemCardFavoriteEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        val event = getItem(position)
        holder.apply {
            val eventDate = getDateFormat(event.startsAt, event.timezone)
            var showEventDate = true
            if (position != 0) {
                val previousEvent = getItem(position - 1)
                if (previousEvent != null && eventDate == getDateFormat(previousEvent.startsAt, previousEvent.timezone))
                    showEventDate = false
            }
            bind(event, position, if (showEventDate) eventDate else "")
            eventClickListener = onEventClick
            favFabClickListener = onFavFabClick
        }
    }

    private fun getDateFormat(eventDate: String, timeZone: String): String {
        val date = getEventDateTime(eventDate, timeZone)
        return getFormattedDate(date)
    }
}
