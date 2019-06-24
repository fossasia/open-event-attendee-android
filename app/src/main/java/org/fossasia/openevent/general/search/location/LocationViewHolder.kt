package org.fossasia.openevent.general.search.location

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_location_text.view.locationText

class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(location: String, listener: TextClickListener?) {
        itemView.locationText.text = location
        itemView.setOnClickListener {
            listener?.onTextClick(location)
        }
    }
}
