package org.fossasia.openevent.general.event

import android.arch.persistence.room.TypeConverter

class EventConverter {

    @TypeConverter
    fun fromEvent(eventId: EventId): Long{
        return eventId.id
    }

    @TypeConverter
    fun toEvent(id: Long): EventId{
        return EventId(id)
    }
}