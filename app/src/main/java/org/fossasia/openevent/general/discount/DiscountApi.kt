package org.fossasia.openevent.general.discount

import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import retrofit2.http.GET
import retrofit2.http.Path

interface DiscountApi {

    @GET("events/{id}/discount-code?include=event&fields[event]=id")
    fun getDiscountCodes(@Path("id") id: Long): Single<DiscountCode>
}
