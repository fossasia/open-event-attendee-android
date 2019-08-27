package org.fossasia.openevent.general.attendees

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class AttendeeConverter {

    @TypeConverter
    fun toAttendee(json: String): Attendee? {
        return jacksonObjectMapper().readerFor(Attendee::class.java).readValue<Attendee>(json)
    }

    @TypeConverter
    fun toJson(attendee: Attendee?) = ObjectMapper().writeValueAsString(attendee)
}
