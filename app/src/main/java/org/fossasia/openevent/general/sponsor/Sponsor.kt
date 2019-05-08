package org.fossasia.openevent.general.sponsor

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("sponsor")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Sponsor(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String?,
    val description: String?,
    val url: String?,
    val logoUrl: String?,
    val level: Int,
    val type: String?
)
