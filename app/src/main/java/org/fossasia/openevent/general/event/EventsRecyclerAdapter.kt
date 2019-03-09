package org.fossasia.openevent.general.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.item_card_events.view.shareFab
import kotlinx.android.synthetic.main.item_card_events.view.favoriteFab
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R
import java.util.ArrayList

class EventsRecyclerAdapter : RecyclerView.Adapter<EventViewHolder>() {
    private val events = ArrayList<Event>()
    private var clickListener: RecyclerViewClickListener? = null
    private var favoriteFabListener: FavoriteFabListener? = null
    private var eventLayout: String? = null

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun setFavorite(listener: FavoriteFabListener) {
        favoriteFabListener = listener
    }

    fun setEventLayout(type: String) {
        eventLayout = type
    }

    fun addAll(eventList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
    }

    fun removeAll() {
        events.clear()
    }

    fun getPos(id: Long): Int {
        return events.map { it.id }.indexOf(id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val eventView: View = if (eventLayout.equals(SIMILAR_EVENTS)) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_similar_events, parent,
                false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent,
                false)
        }
        eventView.shareFab.scaleType = ImageView.ScaleType.CENTER
        eventView.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        return EventViewHolder(eventView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, clickListener, favoriteFabListener, EVENT_DATE_FORMAT)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}

interface FavoriteFabListener {
    fun onClick(event: Event, isFavorite: Boolean)
}
