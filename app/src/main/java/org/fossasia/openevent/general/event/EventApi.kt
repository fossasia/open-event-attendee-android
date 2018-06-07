package org.fossasia.openevent.general.event

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface EventApi {

    @GET("events")
    fun getEvents(): Single<List<Event>>

    @GET("events")
    fun searchEvents(@Query("sort") sort: String, @Query("filter") eventName: String): Single<List<Event>>

    @GET
    fun getEvent(id: Long): Single<Event>

}