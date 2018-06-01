package org.fossasia.openevent.general.event

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*

class EventsRecyclerAdapter : RecyclerView.Adapter<EventViewHolder>() {
    private val events = ArrayList<Event>()

    fun addAll(eventList: List<Event>) {
        this.events.addAll(eventList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}