package org.fossasia.openevent.general.event

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*

class EventsRecyclerAdapter : RecyclerView.Adapter<EventViewHolder>() {
    private val events = ArrayList<Event>()
    private var clickListener: RecyclerViewClickListener? = null
    private var favouriteFabClickListener: FavouriteFabClickListener? = null

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun setFavouriteListener(listener: FavouriteFabClickListener) {
        favouriteFabClickListener = listener
    }

    fun addAll(eventList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, clickListener, favouriteFabClickListener)
    }

    override fun getItemCount(): Int {
        return events.size
    }

}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}

interface FavouriteFabClickListener {
    fun onClick(eventId: Long, isFavourite: Boolean)
}