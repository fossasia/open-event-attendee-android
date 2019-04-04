package org.fossasia.openevent.general.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersViewHolder>() {

    private val eventAndOrderIdentifier = ArrayList<Pair<Event, String>>()
    private val showExpired = false
    private var clickListener: OrderClickListener? = null
    var attendeesNumber = listOf<Int>()

    fun setListener(listener: OrderClickListener) {
        clickListener = listener
    }

    fun addAllPairs(list: List<Pair<Event, String>>, showExpired: Boolean) {
        if (eventAndOrderIdentifier.isNotEmpty())
            this.eventAndOrderIdentifier.clear()
        eventAndOrderIdentifier.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_order, parent, false)
        return OrdersViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        attendeesNumber[position]?.let {
            holder.bind(eventAndOrderIdentifier[position].first,
                clickListener,
                eventAndOrderIdentifier[position].second,
                it, showExpired)
        }
    }

    override fun getItemCount(): Int {
        return eventAndOrderIdentifier.size
    }

    fun setAttendeeNumber(number: List<Int>) {
        attendeesNumber = number
    }

    interface OrderClickListener {
        fun onClick(eventID: Long, orderIdentifier: String)
    }
}
