package org.fossasia.openevent.general.social

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event

@Type("social-link")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["event"], onDelete = CASCADE))])
data class SocialLink(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val link: String,
        val name: String,
        @Relationship("event")
        var event: Event?
)