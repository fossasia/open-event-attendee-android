package org.fossasia.openevent.general.discount

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscountApi {

    @GET("discount-codes/{code}?include=event,tickets")
    fun getDiscountCodes(@Path("code") code: String, @Query("filter") filter: String): Single<DiscountCode>
}
