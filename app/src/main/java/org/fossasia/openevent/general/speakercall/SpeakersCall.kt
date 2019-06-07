package org.fossasia.openevent.general.speakercall

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("speakers-call")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class SpeakersCall(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val announcement: String,
    val startsAt: String,
    val endsAt: String,
    val hash: String?,
    val privacy: String?
)
