package org.fossasia.openevent.general.search.location

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import kotlinx.android.synthetic.main.item_place_suggestion.view.placeName
import kotlinx.android.synthetic.main.item_place_suggestion.view.subPlaceName

/**
 * The [RecyclerView.ViewHolder] class for Place Suggestions items list in [SearchLocationFragment]
 * It implements the LayoutContainer interface from Kotlin Android Extensions to cache view lookup calls.
 */

class PlaceSuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var onSuggestionClick: ((String) -> Unit)? = null

    fun bind(carmenFeature: CarmenFeature) {
        carmenFeature.placeName()?.let {
            val placeDetails = extractPlaceDetails(it)
            itemView.placeName.text = placeDetails.first
            itemView.subPlaceName.text = placeDetails.second
            itemView.subPlaceName.isVisible = placeDetails.second.isNotEmpty()

            itemView.setOnClickListener {
                onSuggestionClick?.invoke(placeDetails.first)
            }
        }
    }

    private fun extractPlaceDetails(placeName: String): Pair<String, String> {
        val list = placeName.split(",")
        val mainPlaceDetail = list.first()
        val subPlaceDetail = list.subList(1, list.size).joinToString(", ")

        return mainPlaceDetail to subPlaceDetail
    }
}
