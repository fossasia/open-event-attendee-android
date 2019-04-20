package org.fossasia.openevent.general.event.topic

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper

class EventTopicConverter {
    @TypeConverter
    fun toEventTopic(json: String): EventTopic? {
        return ObjectMapper().readerFor(EventTopic::class.java).readValue<EventTopic>(json)
    }

        @TypeConverter
    fun toJson(eventTopic: EventTopic?) = ObjectMapper().writeValueAsString(eventTopic)
}
