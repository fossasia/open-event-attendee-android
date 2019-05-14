package org.fossasia.openevent.general.attendees

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class ListAttendeeIdConverter {

    @TypeConverter
    fun fromListAttendeeId(attendeeIdList: List<AttendeeId>): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(attendeeIdList)
    }

    @TypeConverter
    fun toListAttendeeId(attendeeList: String): List<AttendeeId> {
        val objectMapper = jacksonObjectMapper()
        val mapType = object : TypeReference<List<AttendeeId>>() {}
        return objectMapper.readValue(attendeeList, mapType)
    }
}
