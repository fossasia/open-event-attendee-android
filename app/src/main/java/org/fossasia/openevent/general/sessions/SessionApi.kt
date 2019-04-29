package org.fossasia.openevent.general.sessions

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @GET("events/{eventId}/sessions?include=session-type,microlocation")
    fun getSessionsForEvent(
        @Path("eventId") eventId: Long,
        @Query("sort") sort: String = "created-at",
        @Query("filter") sessionName: String = "[]"
    ): Single<List<Session>>
}
