package org.fossasia.openevent.general.order

import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("charge")
data class Charge(
        @Id(IntegerIdHandler::class)
        val id: String,
        val stripe: String? = null,
        val paypal: String? = null
)