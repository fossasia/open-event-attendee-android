package org.fossasia.openevent.general.sessions.track

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("track")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Track(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String?,
    val color: String,
    val fontColor: String?
)
