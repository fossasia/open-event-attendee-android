package org.fossasia.openevent.general.attendees

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface AttendeeApi {

    @POST("attendees?include=event,ticket&fields[event]=id&fields[ticket]=id")
    fun postAttendee(@Body attendee: AttendeeModel): Single<Attendee>

}