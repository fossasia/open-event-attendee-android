package org.fossasia.openevent.general.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.reactivex.annotations.NonNull

@Type("notification")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Notification(
    @Id(IntegerIdHandler::class)
    @NonNull
    @PrimaryKey
    val id: Int,
    val message: String? = null,
    val receivedAt: String? = null,
    @get:JsonProperty("is-read")
    var isRead: Boolean = false,
    val title: String? = null,
    val deletedAt: String? = null
)
