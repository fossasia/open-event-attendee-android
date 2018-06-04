package org.fossasia.openevent.general.discount

import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import retrofit2.http.GET
import retrofit2.http.Path

interface DiscountApi {

    @GET("events/{event_identifier}/discount-code?include=event")
    fun getDiscountCodes(@Path("event_identifier") id: Long): Single<DiscountCode>

}