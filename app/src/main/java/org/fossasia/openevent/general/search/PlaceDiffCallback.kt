package org.fossasia.openevent.general.search

import androidx.recyclerview.widget.DiffUtil
import com.mapbox.api.geocoding.v5.models.CarmenFeature

/**
 * The DiffUtil ItemCallback class for the [CarmenFeature] model class.
 * This enables proper diffing of items in Recycler Views using [DiffUtil]
 */

class PlaceDiffCallback : DiffUtil.ItemCallback<CarmenFeature>() {
    override fun areItemsTheSame(oldItem: CarmenFeature, newItem: CarmenFeature): Boolean {
        return oldItem.placeName() == newItem.placeName()
    }

    override fun areContentsTheSame(oldItem: CarmenFeature, newItem: CarmenFeature): Boolean {
        return oldItem == newItem
    }
}
