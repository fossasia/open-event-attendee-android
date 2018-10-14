package org.fossasia.openevent.general.attendees

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("attendee")
data class AttendeeId(
    @Id(LongIdHandler::class)
    val id: Long
)
