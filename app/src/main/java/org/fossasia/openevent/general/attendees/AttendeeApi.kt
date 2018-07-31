package org.fossasia.openevent.general.attendees

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface AttendeeApi {

    @POST("attendees?include=event,ticket&fields[event]=id&fields[ticket]=id")
    fun postAttendee(@Body attendee: Attendee): Single<Attendee>

    @DELETE("attendees/{attendeeId}")
    fun deleteAttendee(@Path("attendeeId") id: Long): Completable
}