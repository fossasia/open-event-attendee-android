package org.fossasia.openevent.general.attendees

import androidx.room.TypeConverter

class AttendeeIdConverter {

    @TypeConverter
    fun fromAttendeeId(attendeeId: AttendeeId): Long {
        return attendeeId.id
    }

    @TypeConverter
    fun toAttendeeId(id: Long): AttendeeId {
        return AttendeeId(id)
    }
}
