package org.fossasia.openevent.general

import android.arch.persistence.room.TypeConverter
import org.fossasia.openevent.general.event.EventId
import timber.log.Timber

class EventConverter {

    @TypeConverter
    fun fromEvent(eventId: EventId): Long{
        Timber.d("HELLO " + eventId.id)
        return eventId.id
    }

    @TypeConverter
    fun toEvent(id: Long): EventId{
        val eventId = EventId(id)
        return eventId
    }
}