package org.fossasia.openevent.general.speakers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.fossasia.openevent.general.event.Event

@Entity(
    primaryKeys = ["event_id", "speaker_id"],
    indices = [
        Index(value = ["event_id"]),
        Index(value = ["speaker_id"])
    ],
    foreignKeys = [
        ForeignKey(entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["event_id"]),
        ForeignKey(entity = Speaker::class,
            parentColumns = ["id"],
            childColumns = ["speaker_id"])
    ])
data class SpeakerWithEvent(
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "speaker_id") val speakerId: Long
)
