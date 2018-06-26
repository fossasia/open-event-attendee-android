package org.fossasia.openevent.general.attendees


import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.ticket.TicketId


@Type("attendee")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class AttendeeModel(
        @Id(IntegerIdHandler::class)
        val id: Int,
        var firstname: String? = null,
        var lastname: String? = null,
        var email: String? = null,
        var address: Float? = null,
        var city: Int? = null,
        var state: String? = null,
        var country: String? = null,
        var isCheckedIn: Boolean? = false,
        var pdfUrl: String? = null,
        @Relationship("event")
        var event: EventId? = null,
        @Relationship("ticket")
        var ticket: TicketId? = null
)