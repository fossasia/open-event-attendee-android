package org.fossasia.openevent.general.paypal

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PaypalApi {

    @POST("orders/{orderIdentifier}/verify-mobile-paypal-payment")
    fun verifyPaypalPayment(
        @Path("orderIdentifier") orderIdentifier: String,
        @Body paypal: Paypal
    ): Single<PaypalPaymentResponse>
}
