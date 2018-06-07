package org.fossasia.openevent.general.event

import android.arch.persistence.room.*
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE

@Type("ticket")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity(foreignKeys = arrayOf(ForeignKey(entity = Event::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("id"),
                onDelete = CASCADE)))
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
        val salesStartsAt: String?
)