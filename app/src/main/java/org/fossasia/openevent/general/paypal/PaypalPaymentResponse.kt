package org.fossasia.openevent.general.paypal

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PaypalPaymentResponse(
    val status: Boolean,
    val error: String? = null
)
