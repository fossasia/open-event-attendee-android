package org.fossasia.openevent.general.discount

import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import retrofit2.http.GET

interface DiscountApi {

    @GET("discount-codes?size=0")
    fun getDiscountCodes(): Single<List<DiscountCode>>

}