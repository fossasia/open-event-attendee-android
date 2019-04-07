package org.fossasia.openevent.general.event.types

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event-type")
@Entity
data class EventType(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long?,
    val name: String?,
    val slug: String?
)
