package org.fossasia.openevent.general.event

import io.reactivex.Single
import retrofit2.http.GET

interface EventApi {

    @GET("events")
    fun getEvents(): Single<List<Event>>

    @GET
    fun getEvent(id: Long): Single<Event>

}