package org.fossasia.openevent.general.favorite

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.item_card_favorite_event.view.shareFab
import kotlinx.android.synthetic.main.item_card_favorite_event.view.favoriteFab
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventViewHolder
import org.fossasia.openevent.general.event.FavoriteFabListener
import org.fossasia.openevent.general.event.RecyclerViewClickListener

class FavoriteEventsRecyclerAdapter : RecyclerView.Adapter<EventViewHolder>() {
    private val events = ArrayList<Event>()
    private var clickListener: RecyclerViewClickListener? = null
    private var favoriteFabListener: FavoriteFabListener? = null

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun setFavorite(listener: FavoriteFabListener) {
        favoriteFabListener = listener
    }

    fun addAll(eventList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
    }

    fun getPos(id: Long): Int {
        return events.map { it.id }.indexOf(id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_favorite_event, parent, false)
        view.shareFab.scaleType = ImageView.ScaleType.CENTER
        view.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, clickListener, favoriteFabListener, FAVORITE_EVENT_DATE_FORMAT)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}
