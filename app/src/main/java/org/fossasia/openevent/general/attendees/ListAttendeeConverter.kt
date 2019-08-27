package org.fossasia.openevent.general.attendees

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class ListAttendeeConverter {

    @TypeConverter
    fun fromListAttendee(attendees: List<Attendee>): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(attendees)
    }

    @TypeConverter
    fun toListAttendee(attendees: String): List<Attendee> {
        val objectMapper = jacksonObjectMapper()
        val mapType = object : TypeReference<List<Attendee>>() {}
        return objectMapper.readValue(attendees, mapType)
    }
}
