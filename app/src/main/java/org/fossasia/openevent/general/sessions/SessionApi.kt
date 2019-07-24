package org.fossasia.openevent.general.sessions

import io.reactivex.Single
import org.fossasia.openevent.general.speakercall.Proposal
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
        @Query("filter") sessionName: String = "[]"
    ): Single<List<Session>>

    @POST("sessions?include=track,session-type,event,creator,speakers")
    fun createSession(@Body proposal: Proposal): Single<Session>

    @PATCH("sessions/{sessionId}")
    fun updateSession(@Path("sessionId") sessionId: Long, @Body proposal: Proposal): Single<Session>

    @GET("speakers/{speakerId}/sessions?include=session-type,microlocation,track,event")
    fun getSessionsUnderSpeaker(
        @Path("speakerId") speakerId: Long,
        @Query("filter") filter: String
    ): Single<List<Session>>

    @GET("sessions/{sessionId}?include=track,session-type,event,creator,speakers")
    fun getSessionById(@Path("sessionId") sessionId: Long): Single<Session>
}
