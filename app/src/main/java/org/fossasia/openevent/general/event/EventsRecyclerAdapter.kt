package org.fossasia.openevent.general.event

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import java.util.*


class EventsRecyclerAdapter : PagedListAdapter<Event, EventViewHolder>(UserDiffCallback) {
    private val events = ArrayList<Event>()
    private var clickListener: RecyclerViewClickListener? = null
    private var favoriteFabListener: FavoriteFabListener? = null
    private var eventLayout: String? = null

    companion object {
        val UserDiffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun setListener(listener: RecyclerViewClickListener) {
        clickListener = listener
    }

    fun setFavorite(listener: FavoriteFabListener) {
        favoriteFabListener = listener
    }

    fun setEventLayout(type: String) {
        eventLayout = type
    }

    fun addAll(eventList: PagedList<Event>) {
        if (events.isNotEmpty())
            this.events.clear()
        this.events.addAll(eventList)
        submitList(eventList)
    }

    fun addMoreEvents(newEvents: PagedList<Event>) {
        events.addAll(newEvents)
        submitList(newEvents)
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
        val event = getItem(position) ?: return
        holder.bind(event, clickListener, favoriteFabListener, EVENT_DATE_FORMAT)
    }
}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}

interface FavoriteFabListener {
    fun onClick(event: Event, isFavourite: Boolean)
}