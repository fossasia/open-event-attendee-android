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
    private var idEvent: Long = -1
    private lateinit var eventView: View

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun setFavorite(listener: FavoriteFabListener) {
        favoriteFabListener = listener
    }

    fun setEventLayout(type: String) {
        eventLayout = type
    }

    fun setEventId(id: Long) {
        idEvent = id
    }

    fun addAll(eventList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        if (eventLayout.equals(SIMILAR_EVENTS)) {
            eventView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_similar_events, parent, false)
        } else {
            eventView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        }
        return EventViewHolder(eventView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        handleEventVisibility(event, holder)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    private fun handleEventVisibility(event: Event, holder: EventViewHolder) {
        if (eventLayout.equals(SIMILAR_EVENTS)) {
            if (idEvent == event.id) {
                eventView.layoutParams = RecyclerView.LayoutParams(0, 0)
                eventView.visibility = View.GONE
            } else {
                holder.bind(event, clickListener, favoriteFabListener)
            }
        } else {
            holder.bind(event, clickListener, favoriteFabListener)
        }
    }
}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}

interface FavoriteFabListener {
    fun onClick(eventId: Long, isFavourite: Boolean)
}