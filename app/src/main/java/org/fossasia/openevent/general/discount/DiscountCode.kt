package org.fossasia.openevent.general.auth

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event

@Type("discount-code")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class DiscountCode(
    @Id(IntegerIdHandler::class)
    val id: Int,
    val code: String,
    val discountUrl: String? = null,
    val type: String,
    val value: Float,
    val ticketsNumber: Int?,
    val usedFor: String,
    val tickets: String? = null,
    val maxQuantity: Int?,
    val minQuantity: Int?,
    val isActive: Boolean = false,
    val validFrom: String? = null,
    val validTill: String? = null,
    val createdAt: String? = null,
    @Relationship("event")
    val event: Event
)
