package org.fossasia.openevent.general.event.tax

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface TaxApi {

    @GET("events/{event_identifier}/tax?include=event")
    fun getTaxDetails(@Path("event_identifier") identifier: String): Single<Tax>
}
