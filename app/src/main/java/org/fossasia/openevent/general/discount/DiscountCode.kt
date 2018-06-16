package org.fossasia.openevent.general.auth

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

@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["event"], onDelete = CASCADE))])
@Type("discount-code")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class DiscountCode(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val code: String,
        val discountUrl: String? = null,
        val type: String,
        val value: Float,
        val ticketsNumber: Int?,
        val usedFor: String,
        val tickets: String? = null,
        val maxQuantity: Int?,
        val minQuantity: Int?,
        val isActive: Boolean = false,
        val validFrom: String? = null,
        val validTill: String? = null,
        val createdAt: String? = null,
        @ColumnInfo(index = true)
        @Relationship("event")
        var event: EventId? = null
)