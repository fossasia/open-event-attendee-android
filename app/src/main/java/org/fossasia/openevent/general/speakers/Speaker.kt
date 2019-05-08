package org.fossasia.openevent.general.speakers

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("speaker")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Speaker(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    val shortBiography: String?,
    val longBiography: String?,
    val speakingExperience: String?,
    val position: String?,
    val mobile: String?,
    val location: String?,
    val country: String?,
    val city: String?,
    val organisation: String?,
    val gender: String?,
    val website: String?,
    val twitter: String?,
    val facebook: String?,
    val linkedin: String?,
    val github: String?,
    val isFeatured: Boolean = false

)
