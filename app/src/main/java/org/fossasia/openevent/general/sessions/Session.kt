package org.fossasia.openevent.general.sessions

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.sessions.microlocation.MicroLocation
import org.fossasia.openevent.general.sessions.sessiontype.SessionType
import org.fossasia.openevent.general.sessions.track.Track

@Type("session")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Session(
    @Id(LongIdHandler::class)
    @PrimaryKey
    @NonNull
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
    @ColumnInfo(index = true)
    @Relationship("session-type", resolve = true)
    val sessionType: SessionType? = null,
    @ColumnInfo(index = true)
    @Relationship("microlocation", resolve = true)
    val microlocation: MicroLocation? = null,
    @ColumnInfo(index = true)
    @Relationship("track", resolve = true)
    val track: Track? = null,
    @ColumnInfo(index = true)
    @Relationship("event", resolve = true)
    val event: EventId? = null
)
