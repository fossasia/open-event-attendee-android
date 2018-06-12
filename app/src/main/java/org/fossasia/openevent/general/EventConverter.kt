package org.fossasia.openevent.general

import android.arch.persistence.room.TypeConverter
import org.fossasia.openevent.general.event.Event
import timber.log.Timber

class EventConverter {

    @TypeConverter
    fun fromEvent(event: Event): Long?{
        Timber.d("HELLO " + event.id)
        return event.id
    }

    @TypeConverter
    fun toEvent(id: Long): Event{
        lateinit var event : Event
        return event
    }
}