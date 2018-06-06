package org.fossasia.openevent.general.event

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction

@Type("ticket")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Ticket(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        var id: Int,
        var description: String?,
        var type: String?,
        var name: String,
        var maxOrder: String?,
        var isFeeAbsorbed: Boolean? = false,
        var isDescriptionVisible: Boolean? = false,
        var price: String?,
        var position: String?,
        var quantity: String?,
        var isHidden: Boolean? = false,
        var salesEndsAt: String?,
        var minOrder: String?,
        var salesStartsAt: String?,
        @Relationship("event")
        @ColumnInfo(typeAffinity = ColumnInfo.LOCALIZED)
        var event: Event
)