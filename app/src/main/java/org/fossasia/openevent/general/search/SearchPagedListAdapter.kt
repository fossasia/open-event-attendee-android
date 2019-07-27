package org.fossasia.openevent.general.search

import org.fossasia.openevent.general.favorite.FavoriteEventViewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.EventsDiffCallback
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.databinding.ItemCardFavoriteEventBinding

class SearchPagedListAdapter : PagedListAdapter<Event, FavoriteEventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val binding = ItemCardFavoriteEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.apply {
                bind(event, position, "")
                eventClickListener = onEventClick
                favFabClickListener = onFavFabClick
            }
        }
    }

    fun clear() {
        this.submitList(null)
    }
}
