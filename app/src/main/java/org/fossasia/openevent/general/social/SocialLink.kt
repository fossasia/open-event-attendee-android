package org.fossasia.openevent.general.social

import android.arch.persistence.room.*
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event

@Type("social-link")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class SocialLink(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val link: String,
        val name: String
)