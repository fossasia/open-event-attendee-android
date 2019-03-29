package org.fossasia.openevent.general.common

import android.widget.ImageView
import org.fossasia.openevent.general.event.Event

/**
 * The callback interface for Event item clicks
 */
interface EventClickListener {
    /**
     * The function to be invoked when an event item is clicked
     *
     * @param eventID The ID of the clicked event
     * @param sharedImage The view for shared element transition

     */
    fun onClick(eventID: Long, sharedImage: ImageView)
}

/**
 * The callback interface for Favorite FAB clicks
 */
interface FavoriteFabClickListener {
    /**
     * The function to be invoked when the fab is clicked
     *
     * @param event The event object for which the fab was clicked
     * @param itemPosition The position of the event object in the adapter
     */
    fun onClick(event: Event, itemPosition: Int)
}

/**
 * The callback interface for Share FAB clicks
 */
interface ShareFabClickListener {
    /**
     * The function to be invoked when the fab is clicked
     *
     * @param event The event object for which the fab was clicked
     */
    fun onClick(event: Event)
}
