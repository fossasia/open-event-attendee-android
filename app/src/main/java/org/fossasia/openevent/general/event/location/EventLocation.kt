package org.fossasia.openevent.general.event.location

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event-location")
data class EventLocation(
    @Id(LongIdHandler::class)
    val id: Long,
    val name: String,
    val slug: String
)
