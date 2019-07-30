package org.fossasia.openevent.general.event.tax

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId

@Type("tax")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
class Tax(
    @Id(IntegerIdHandler::class)
    @PrimaryKey
    val id: Int? = null,
    val name: String? = null,
    val rate: Float? = null,
    val taxId: String? = null,
    val registeredCompany: String? = null,
    val address: String? = null,
    val city: String? = null,
    val stare: String? = null,
    val zip: String? = null,
    val invoiceFooter: String? = null,
    val isInvoiceSend: Boolean = false,
    val isTaxIncludedInPrice: Boolean = false,
    val shouldSendInvoice: Boolean = false,
    @ColumnInfo(index = true)
    @Relationship("event")
    val eventId: EventId? = null
)
