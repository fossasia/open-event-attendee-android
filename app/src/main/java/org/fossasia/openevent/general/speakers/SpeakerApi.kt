package org.fossasia.openevent.general.speakers

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.PATCH

interface SpeakerApi {

    @GET("events/{id}/speakers")
    fun getSpeakerForEvent(
        @Path("id") id: Long,
        @Query("filter") filter: String
    ): Single<List<Speaker>>

    @GET("sessions/{sessionId}/speakers")
    fun getSpeakersForSession(@Path("sessionId") id: Long): Single<List<Speaker>>

    @GET("users/{user_id}/speakers?include=event,user")
    fun getSpeakerForUser(
        @Path("user_id") userId: Long,
        @Query("filter") query: String
    ): Single<List<Speaker>>

    @POST("speakers")
    fun addSpeaker(@Body speaker: Speaker): Single<Speaker>

    @PATCH("speakers/{speakerId}")
    fun updateSpeaker(@Path("speakerId") speakerId: Long, @Body speaker: Speaker): Single<Speaker>
}
