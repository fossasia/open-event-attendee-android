package org.fossasia.openevent.general.event.faq

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("faq")
data class EventFAQ(
    @Id(LongIdHandler::class)
    val id: Long,
    val question: String,
    val answer: String
)
