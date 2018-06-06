package org.fossasia.openevent.general.event

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*

class EventsRecyclerAdapter : PagedListAdapter<Event,EventViewHolder>(EVENT_COMPARATOR) {
    private val events = ArrayList<Event>()
    private var clickListener: RecyclerViewClickListener? = null

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun addAll(eventList: List<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        return EventViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.bind(event, clickListener)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    companion object {
        private val EVENT_COMPARATOR = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                    oldItem == newItem
        }
    }
}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}