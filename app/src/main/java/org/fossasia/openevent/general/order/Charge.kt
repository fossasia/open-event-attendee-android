package org.fossasia.openevent.general.order

import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("charge")
data class Charge(
    @Id(IntegerIdHandler::class)
    val id: Int,
    val stripe: String? = null,
    val paypal: String? = null,
    val paypalPayerId: String? = null,
    val paypalPaymentId: String? = null,
    val message: String? = null,
    val status: Boolean? = null
)
