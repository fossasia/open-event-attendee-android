package org.fossasia.openevent.general.event.topic

import io.reactivex.Single
import org.fossasia.openevent.general.event.Event
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventTopicApi {

    @GET("event-topics/{id}/events?include=event-topic")
    fun getEventsUnderTopicIdPaged(
        @Path("id") id: Long,
        @Query("filter") filter: String,
        @Query("page[number]") page: Int,
        @Query("page[size]") pageSize: Int = 5
    ): Single<List<Event>>
}
