package org.fossasia.openevent.general.event

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("event-topic")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class EventTopic(
        @Id(LongIdHandler::class)
        val id: Long,
        val name: String?,
        val slug: String?,
        @Relationship("event")
        var event: EventId? = null
)