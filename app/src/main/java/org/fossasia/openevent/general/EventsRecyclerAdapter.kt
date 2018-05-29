package org.fossasia.openevent.general

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card_events.view.*
import org.fossasia.openevent.general.model.Event
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class EventsRecyclerAdapter : RecyclerView.Adapter<EventsRecyclerAdapter.EventViewHolder>() {

    private val events = ArrayList<Event>()

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event) {
            itemView.eventName.text = event.name
            itemView.description.text = event.description

            val splitDay = dateFormat(event.startsAt).split(" ")

            itemView.date.text = splitDay[0]
            itemView.month.text = splitDay[1].toUpperCase()
            itemView.year.text = splitDay[2]

            event.originalImageUrl?.let {
                Picasso.get()
                        .load(it)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(itemView.eventImage)
            }
        }

        private fun dateFormat(dateString: String?): String {
            val string = dateString?.substring(0, 9)
            val format = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
            try {
                val date = format.parse(string)
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                return dateFormat.format(date)
            } catch (e: ParseException) {
                Timber.e(e, "Date conversion failed %s", dateString)
            }

            return ""
        }
    }

    fun addAll(eventList: List<Event>) {
        this.events.addAll(eventList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsRecyclerAdapter.EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_events, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsRecyclerAdapter.EventViewHolder, position: Int) {
        val event = events[position]

        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}