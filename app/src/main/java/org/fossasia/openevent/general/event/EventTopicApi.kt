package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

interface EventTopicApi {

    @GET("event-topics/{id}/events")
    fun getEventsUnderTopicId(@Path("id") id: Long): Flowable<List<Event>>

    @GET("event-topics")
    fun getEventTopics(): Flowable<List<EventTopic>>

    @GET("events/{id}/event-topic")
    fun getEventTopicOfEvent(@Path("id") id: Long): Flowable<EventTopic>

    @GET("event-topics/{id}")
    fun getEventTopic(@Path("id") id: Long): Flowable<EventTopic>

}