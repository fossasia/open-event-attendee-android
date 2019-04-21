package org.fossasia.openevent.general.event.topic

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class EventTopicConverter {
    @TypeConverter
    fun toEventTopic(json: String): EventTopic? {
        return jacksonObjectMapper().readerFor(EventTopic::class.java).readValue<EventTopic>(json)
    }

        @TypeConverter
    fun toJson(eventTopic: EventTopic?) = ObjectMapper().writeValueAsString(eventTopic)
}
