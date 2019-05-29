package org.fossasia.openevent.general.order

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.reactivex.annotations.NonNull
import org.fossasia.openevent.general.attendees.AttendeeId
import org.fossasia.openevent.general.event.EventId

@Type("order")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Order(
    @Id(IntegerIdHandler::class)
    @PrimaryKey
    @NonNull
    val id: Long,
    val paymentMode: String? = null,
    val country: String? = null,
    val status: String? = null,
    val amount: Float = 0F,
    val identifier: String? = null,
    val orderNotes: String? = null,
    val completedAt: String? = null,
    val city: String? = null,
    val address: String? = null,
    val createdAt: String? = null,
    val zipcode: String? = null,
    val paidVia: String? = null,
    val discountCodeId: String? = null,
    val ticketsPdfUrl: String? = null,
    val transactionId: String? = null,
    val isBillingEnabled: Boolean = false,
    val taxBusinessInfo: String? = null,
    val company: String? = null,
    @ColumnInfo(index = true)
    @Relationship("event")
    var event: EventId? = null,
    @Relationship("attendees")
    var attendees: List<AttendeeId> = emptyList()
)
