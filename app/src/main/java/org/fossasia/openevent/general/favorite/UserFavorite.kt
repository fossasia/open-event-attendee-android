package org.fossasia.openevent.general.favorite

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId

@Type("user-favourite-event")
data class UserFavorite(
    @Id(LongIdHandler::class)
    val id: Long? = null,
    @Relationship("event", resolve = true)
    var event: EventId? = null
)
