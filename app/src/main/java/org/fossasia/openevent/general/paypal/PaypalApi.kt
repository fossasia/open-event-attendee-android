package org.fossasia.openevent.general.paypal

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PaypalApi {

    @POST("orders/{orderIdentifier}/create-paypal-payment")
    fun createPaypalPayment(@Path("orderIdentifier") orderIdentifier: String, @Body paypal: Paypal)
}
