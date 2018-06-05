package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface EventApi {

    @GET("events")
    fun getEvents(): Single<List<Event>>

    @GET("events")
    fun searchEvents(@Query("filter") eventName: String): Flowable<List<Event>>

    @GET
    fun getEvent(id: Long): Single<Event>

}