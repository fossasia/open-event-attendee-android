package org.fossasia.openevent.general.event

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*

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

    fun getPos(id: Long): Int {
        return events.map { it.id }.indexOf(id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val eventView: View
        if (eventLayout.equals(SIMILAR_EVENTS)) {
            eventView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_similar_events, parent, false)
        } else {
            eventView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        }
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
    fun onClick(event: Event, isFavourite: Boolean)
}
