package org.fossasia.openevent.general.event.subtopic

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper

class EventSubTopicConverter {

    @TypeConverter
    fun toEventSubTopic(json: String): EventSubTopic? {
        return ObjectMapper().readerFor(EventSubTopic::class.java).readValue<EventSubTopic>(json)
    }

    @TypeConverter
    fun toJson(eventSubTopic: EventSubTopic?) = ObjectMapper().writeValueAsString(eventSubTopic)
}
