package org.fossasia.openevent.general

import android.arch.persistence.room.TypeConverter
import org.fossasia.openevent.general.event.Event

class Converter {

    @TypeConverter
    fun fromEvent(event: Event?): Long?{
        return event?.id
    }

    @TypeConverter
    fun toEvent(id: Long): Event?{
        lateinit var event : Event
        return event
    }
}