package org.fossasia.openevent.general.ticket

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("ticket")
data class TicketId(
    @Id(LongIdHandler::class)
    val id: Long
)
