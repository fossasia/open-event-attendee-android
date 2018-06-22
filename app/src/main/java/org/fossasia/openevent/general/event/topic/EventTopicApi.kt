package org.fossasia.openevent.general.event.topic

import io.reactivex.Single
import org.fossasia.openevent.general.event.Event
import retrofit2.http.GET
import retrofit2.http.Path

interface EventTopicApi {

    @GET("event-topics/{id}/events?include=event-topic")
    fun getEventsUnderTopicId(@Path("id") id: Long): Single<List<Event>>

    @GET("event-topics")
    fun getEventTopics(): Single<List<EventTopic>>

    @GET("events/{id}/event-topic")
    fun getEventTopicOfEvent(@Path("id") id: Long): Single<EventTopic>

    @GET("event-topics/{id}")
    fun getEventTopic(@Path("id") id: Long): Single<EventTopic>

}