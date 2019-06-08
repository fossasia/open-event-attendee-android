package org.fossasia.openevent.general.feedback

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApi {

    @GET("events/{eventId}/feedbacks?include=event")
    fun getEventFeedback(
        @Path("eventId") eventId: Long,
        @Query("sort") sort: String = "rating",
        @Query("filter") eventName: String = "[]"
    ): Single<List<Feedback>>

    @POST("feedbacks")
    fun postfeedback(@Body feedback: Feedback): Single<Feedback>
}
