package org.fossasia.openevent.general.sessions

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PATCH

interface SessionApi {

    @GET("events/{eventId}/sessions?include=session-type,microlocation,track,event")
    fun getSessionsForEvent(
        @Path("eventId") eventId: Long,
        @Query("sort") sort: String = "created-at",
        @Query("filter") filter: String = "[]"
    ): Single<List<Session>>

    @POST("sessions")
    fun createSession(@Body session: Session): Single<Session>

    @PATCH("sessions/{sessionId}")
    fun updateSession(@Path("sessionId") sessionId: Long, @Body session: Session): Single<Session>

    @GET("speakers/{speakerId}/sessions?include=session-type,microlocation,track,event")
    fun getSessionsUnderSpeaker(
        @Path("speakerId") speakerId: Long,
        @Query("filter") filter: String
    ): Single<List<Session>>
}
