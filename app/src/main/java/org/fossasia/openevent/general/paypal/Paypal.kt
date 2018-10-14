package org.fossasia.openevent.general.paypal

import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("paypal-payment")
data class Paypal(
    @Id(IntegerIdHandler::class)
    val id: Int,
    val cancelUrl: String? = null,
    val returnUrl: String? = null
)
