package org.fossasia.openevent.general.event

import io.reactivex.Single
import org.fossasia.openevent.general.speakercall.SpeakersCall
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {

    @GET("events?include=event-sub-topic,event-topic,event-type,speakers-call")
    fun searchEvents(@Query("sort") sort: String, @Query("filter") eventName: String): Single<List<Event>>

    @GET
    fun getEvent(id: Long): Single<Event>

    @GET("/v1/events/{eventIdentifier}")
    fun getEventFromApi(@Path("eventIdentifier") eventIdentifier: String): Single<Event>

    @GET("events")
    fun eventsUnderUser(@Query("filter") eventId: String): Single<List<Event>>

    @GET("events/{eventId}/speakers-call")
    fun getSpeakerCallForEvent(@Path("eventId") id: Long): Single<SpeakersCall>

    @GET("events?include=event-sub-topic,event-topic,event-type")
    fun searchEventsPaged(
        @Query("sort") sort: String,
        @Query("filter") eventName: String,
        @Query("page[number]") page: Int,
        @Query("page[size]") pageSize: Int = 5
    ): Single<List<Event>>
}
