package org.fossasia.openevent.general.event

import io.reactivex.Observable
import retrofit2.http.GET

interface EventApi {

    @GET("events")
    fun getEvents(): Observable<List<Event>>

}