package org.fossasia.openevent.general.sessions.sessiontype

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("session-type")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class SessionType(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val length: String?,
    val deletedAt: String?
)
