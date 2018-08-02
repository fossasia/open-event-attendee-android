package org.fossasia.openevent.general.attendees.forms

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId

@Type("custom-form")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class CustomForm(
        @Id(IntegerIdHandler::class)
        val id: Long,
        val form: String,
        val fieldIdentifier: String,
        val type: String,
        val isRequired: Boolean? = false,
        val isIncluded: Boolean? = false,
        val isFixed: Boolean? = false,
        val ticketsNumber: Int? = null,
        @Relationship("event")
        var event: EventId? = null
)