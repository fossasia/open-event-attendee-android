package org.fossasia.openevent.general.sessions.microlocation

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("microlocation")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class MicroLocation(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val room: String?,
    val latitude: String?,
    val longitude: String?,
    val floor: String?,
    val deletedAt: String?
)
