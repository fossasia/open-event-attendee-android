package org.fossasia.openevent.general.event

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Relation
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("ticket")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
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
        val isHidden: Boolean? = false,
        val salesEndsAt: String?,
        val minOrder: String?,
        val salesStartsAt: String?,
        @Relationship("event")
        val event: Event
)