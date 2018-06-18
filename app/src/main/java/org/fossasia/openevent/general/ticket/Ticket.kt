package org.fossasia.openevent.general.ticket

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId

@Type("ticket")
@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["event"], onDelete = CASCADE))])
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Ticket(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val description: String?,
        val type: String?,
        val name: String,
        val maxOrder: String?,
        val isFeeAbsorbed: Boolean? = false,
        val isDescriptionVisible: Boolean? = false,
        val price: String?,
        val position: String?,
        val quantity: String?,

        val isHidden: Boolean?,
        val salesStartsAt: String?,
        val salesEndsAt: String?,
        val minOrder: String?,
        @ColumnInfo(index = true)
        @Relationship("event")
        var event: EventId? = null
)

