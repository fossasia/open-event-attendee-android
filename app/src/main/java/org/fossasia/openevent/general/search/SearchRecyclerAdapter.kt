package org.fossasia.openevent.general.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import java.util.*

class SearchRecyclerAdapter : RecyclerView.Adapter<SearchViewHolder>() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_search_events, parent, false)
        return SearchViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val event = events[position]

        holder.bind(event, clickListener)
    }

    override fun getItemCount(): Int {
        return events.size
    }

}

interface RecyclerViewClickListener {
    fun onClick(eventID: Long)
}