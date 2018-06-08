package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface TicketApi {

    @GET("events/{id}/tickets")
    fun getTickets(@Path("id") id: Long): Single<List<Ticket>>

}