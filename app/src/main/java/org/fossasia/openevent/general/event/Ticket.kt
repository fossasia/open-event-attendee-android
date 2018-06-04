package org.fossasia.openevent.general.event

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("ticket")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Ticket(
        @Id(IntegerIdHandler::class)
        val id: Int,
        val description: String? = null,
        val type: String,
        val name: String,
        val maxOrder: String? = null,
        val isFeeAbsorbed: Boolean? = false,
        val isDescriptionVisible: Boolean? = false,
        val price: String? = null,
        val position: String? = null,
        val quantity: String? = null,
        val isHidden: Boolean? = false,
        val salesEndsAt: String,
        val minOrder: String? = null,
        val salesStartsAt: String
)