package org.fossasia.openevent.general.sessions.sessiontype

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class SessionTypeConverter {

    @TypeConverter
    fun toSessionType(json: String): SessionType? =
        jacksonObjectMapper().readerFor(SessionType::class.java).readValue<SessionType>(json)

    @TypeConverter
    fun toJson(sessionType: SessionType?) = ObjectMapper().writeValueAsString(sessionType)
}
