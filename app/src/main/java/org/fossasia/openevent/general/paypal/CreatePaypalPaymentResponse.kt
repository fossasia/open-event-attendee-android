package org.fossasia.openevent.general.paypal

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class CreatePaypalPaymentResponse(
    val status: Boolean,
    val paymentId: String,
    val error: String
)
