package org.fossasia.openevent.general.event.types

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class EventTypeConverter {
    @TypeConverter
    fun toEventType(json: String): EventType? {
        return jacksonObjectMapper().readerFor(EventType::class.java).readValue<EventType>(json)
    }

    @TypeConverter
    fun toJson(eventType: EventType?) = ObjectMapper().writeValueAsString(eventType)
}
