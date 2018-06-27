package org.fossasia.openevent.general.attendees

import io.reactivex.Single

class AttendeeService(private val attendeeApi: AttendeeApi, private val attendeeDao: AttendeeDao) {
    fun postAttendee(attendee: Attendee): Single<Attendee> {
        return attendeeApi.postAttendee(attendee)
                .map {
                    attendeeDao.insertAttendee(it)
                    it
                }
    }
}