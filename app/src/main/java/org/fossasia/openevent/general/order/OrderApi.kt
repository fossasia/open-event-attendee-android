package org.fossasia.openevent.general.order

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApi {

    @POST("orders?include=event,attendee&fields[event]=id&fields[attendee]=id")
    fun placeOrder(@Body order: Order): Single<Order>

}