package org.fossasia.openevent.general.attendees

import android.arch.persistence.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

class ListAttendeeIdConverter {

    @TypeConverter
    fun fromListAttendeeId(attendeeIdList: List<AttendeeId>): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(attendeeIdList)
    }

    @TypeConverter
    fun toListAttendeeId(attendeeList: String): List<AttendeeId> {
        val objectMapper = ObjectMapper()
        val mapType = object : TypeReference<List<AttendeeId>>() {}
        return objectMapper.readValue(attendeeList, mapType)
    }
}
