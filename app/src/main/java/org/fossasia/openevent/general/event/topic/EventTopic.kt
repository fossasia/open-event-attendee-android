package org.fossasia.openevent.general.event.topic

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId

@Type("event-topic")
@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["event"], onDelete = ForeignKey.CASCADE))])
data class EventTopic(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long?,
    val name: String?,
    val slug: String?,
    @ColumnInfo(index = true)
    @Relationship("event")
    var event: EventId? = null
)
