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
     * @param imageView The Image View of event object in the adapter
     */
    fun onClick(eventID: Long, imageView: ImageView)
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
 * The callback interface for Speaker item clicks
 */
interface SpeakerClickListener {
    /**
     * The function to be invoked when a speaker item is clicked
     *
     * @param speakerId The ID of the clicked speaker
     */
    fun onClick(speakerId: Long)
}

/**
 * The callback interface for Speaker item clicks
 */
interface SessionClickListener {
    /**
     * The function to be invoked when a speaker item is clicked
     *
     * @param sessionId The ID of the clicked session
     */
    fun onClick(sessionId: Long)
}
