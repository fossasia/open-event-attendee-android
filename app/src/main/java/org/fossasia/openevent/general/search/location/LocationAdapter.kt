package org.fossasia.openevent.general.search.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R

class LocationsAdapter : RecyclerView.Adapter<LocationViewHolder>() {
    private val locationsList = mutableListOf<String>()
    private var listener: TextClickListener? = null

    fun addAll(locations: List<String>) {
        if (locationsList.isNotEmpty()) locationsList.clear()
        locationsList.addAll(locations)
        notifyDataSetChanged()
    }

    fun setListener(listener: TextClickListener) {
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location_text, parent, false)
        return LocationViewHolder(view)
    }

    override fun getItemCount(): Int = locationsList.size

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locationsList[position], listener)
    }
}

interface TextClickListener {
    fun onTextClick(location: String)
}
