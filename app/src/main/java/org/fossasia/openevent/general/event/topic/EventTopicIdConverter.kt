package org.fossasia.openevent.general.event.topic

import android.arch.persistence.room.TypeConverter

class EventTopicIdConverter {

    @TypeConverter
    fun fromEventTopic(eventTopic: EventTopic?): Long? {
        return eventTopic?.id ?: -1
    }

    @TypeConverter
    fun toEventTopic(id: Long?): EventTopic {
        return if (id == null) {
            EventTopic(-1, "", "")
        } else {
            EventTopic(id, "", "")
        }
    }
}
