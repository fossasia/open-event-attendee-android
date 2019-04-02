package org.fossasia.openevent.general.favorite

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import kotlinx.android.synthetic.main.item_card_favorite_event.view.shareFab
import kotlinx.android.synthetic.main.item_card_favorite_event.view.favoriteFab
import kotlinx.android.synthetic.main.item_card_favorite_event.view.eventImage
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.event.EventViewHolder
import org.fossasia.openevent.general.common.EventsDiffCallback
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener

/**
 * The RecyclerView adapter class for displaying favorite events list
 *
 * @param diffCallback The DiffUtil.ItemCallback implementation to be used with this adapter.
 * @property onEventClick The callback to be invoked when an event is clicked
 * @property onFavFabClick The callback to be invoked when the favorite FAB is clicked
 * @property onShareFabClick The callback to be invoked when the share FAB is clicked
 */
class FavoriteEventsRecyclerAdapter(
    diffCallback: EventsDiffCallback
) : ListAdapter<Event, EventViewHolder>(diffCallback) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null
    var onShareFabClick: ShareFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_favorite_event, parent, false)
        view.shareFab.scaleType = ImageView.ScaleType.CENTER
        view.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.eventImage.clipToOutline = true
        }
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.apply {
            bind(event, FAVORITE_EVENT_DATE_FORMAT)
            eventClickListener = onEventClick
            favFabClickListener = onFavFabClick
            shareFabClickListener = onShareFabClick
        }
    }
}
