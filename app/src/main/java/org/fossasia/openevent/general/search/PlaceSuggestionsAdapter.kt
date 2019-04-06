package org.fossasia.openevent.general.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import org.fossasia.openevent.general.R

/**
 * The RecyclerView adapter class for displaying lists of Place Suggestions.
 *
 * @param diffCallback The DiffUtil.ItemCallback implementation to be used with this adapter
 */

class PlaceSuggestionsAdapter(diffCallback: PlaceDiffCallback) :
    ListAdapter<CarmenFeature,
        PlaceSuggestionViewHolder>(diffCallback) {

    var onSuggestionClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceSuggestionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_suggestion, parent, false)
        return PlaceSuggestionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceSuggestionViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
            onSuggestionClick = this@PlaceSuggestionsAdapter.onSuggestionClick
        }
    }
}
