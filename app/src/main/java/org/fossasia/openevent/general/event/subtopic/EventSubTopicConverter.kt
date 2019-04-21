package org.fossasia.openevent.general.event.subtopic

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class EventSubTopicConverter {

    @TypeConverter
    fun toEventSubTopic(json: String): EventSubTopic? {
        return jacksonObjectMapper().readerFor(EventSubTopic::class.java).readValue<EventSubTopic>(json)
    }

    @TypeConverter
    fun toJson(eventSubTopic: EventSubTopic?) = ObjectMapper().writeValueAsString(eventSubTopic)
}
