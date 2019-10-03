package org.fossasia.openevent.general.paypal

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("order")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
class Paypal(
    @Id
    val id: Int? = null,
    val paymentId: String
)
