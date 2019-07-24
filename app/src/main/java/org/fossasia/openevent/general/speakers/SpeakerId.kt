package org.fossasia.openevent.general.speakers

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("speaker")
data class SpeakerId(
    @Id(LongIdHandler::class)
    val id: Long
)
