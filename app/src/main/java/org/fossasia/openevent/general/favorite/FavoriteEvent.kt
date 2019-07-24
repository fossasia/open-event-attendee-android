package org.fossasia.openevent.general.favorite

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId

@Type("user-favourite-event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class FavoriteEvent(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    @ColumnInfo(index = true)
    @Relationship("event", resolve = true)
    val event: EventId? = null
)
