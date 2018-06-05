package org.fossasia.openevent.general.event

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("ticket")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Ticket(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val description: String? = null,
        val type: String? = null,
        val name: String,
        val maxOrder: String? = null,
        val isFeeAbsorbed: Boolean? = false,
        val isDescriptionVisible: Boolean? = false,
        val price: String? = null,
        val position: String? = null,
        val quantity: String? = null,
        val isHidden: Boolean? = false,
        val salesEndsAt: String? = null,
        val minOrder: String? = null,
        val salesStartsAt: String? = null
)