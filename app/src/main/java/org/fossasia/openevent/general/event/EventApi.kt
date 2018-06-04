package org.fossasia.openevent.general.event

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface EventApi {

    @GET("events")
    fun getEvents(): Single<List<Event>>

    @GET
    fun getEvent(id: Long): Single<Event>

    @GET("events/{identifier}/tickets")
    fun getTickets(@Path("identifier") identifier: String): Single<List<Ticket>>

}