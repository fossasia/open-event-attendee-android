package org.fossasia.openevent.general.sessions

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("session-type")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class SessionType(
    @Id(LongIdHandler::class)
    val id: Long,
    val name: String,
    val length: String?,
    val deletedAt: String?
)
