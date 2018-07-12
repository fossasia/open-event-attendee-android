package org.fossasia.openevent.general.order

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    @POST("orders")
    fun placeOrder(@Body order: Order): Single<Order>

    @POST("orders/{orderIdentifier}/charge")
    fun chargeOrder(@Path("orderIdentifier") orderIdentifier: String, @Body charge: Charge): Single<Charge>

}