package org.fossasia.openevent.general.speakers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.auth.UserId
import org.fossasia.openevent.general.event.EventId

@Type("speaker")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Speaker(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val shortBiography: String? = null,
    val longBiography: String? = null,
    val speakingExperience: String? = null,
    val position: String? = null,
    val mobile: String? = null,
    val location: String? = null,
    val country: String? = null,
    val city: String? = null,
    val organisation: String? = null,
    val gender: String? = null,
    val website: String? = null,
    val twitter: String? = null,
    val facebook: String? = null,
    val linkedin: String? = null,
    val github: String? = null,
    val isFeatured: Boolean = false,
    @ColumnInfo(index = true)
    @Relationship("event")
    val event: EventId? = null,
    @ColumnInfo(index = true)
    @Relationship("user")
    val user: UserId? = null

)
