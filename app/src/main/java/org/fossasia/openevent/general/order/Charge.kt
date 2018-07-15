package org.fossasia.openevent.general.order

import com.github.jasminb.jsonapi.annotations.Type

@Type("charge")
data class Charge(
        val stripe: String? = null,
        val paypal: String? = null
)