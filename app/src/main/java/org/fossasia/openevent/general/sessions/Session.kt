package org.fossasia.openevent.general.sessions

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("session")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Session(
    @Id(LongIdHandler::class)
    val id: Long,
    val shortAbstract: String? = null,
    val comments: String? = null,
    val longAbstract: String? = null,
    val level: String? = null,
    val signupUrl: String? = null,
    val endsAt: String? = null,
    val language: String? = null,
    val title: String? = null,
    val startsAt: String? = null,
    val slidesUrl: String? = null,
    val averageRating: Float? = null,
    val submittedAt: String? = null,
    val deletedAt: String? = null,
    val subtitle: String? = null,
    val createdAt: String? = null,
    val state: String? = null,
    val lastModifiedAt: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    @Relationship("session-type", resolve = true)
    var sessionType: SessionType? = null,
    @Relationship("microlocation", resolve = true)
    var microlocation: Microlocation? = null
)
