package org.fossasia.openevent.general.sponsor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.fossasia.openevent.general.event.Event

@Entity(
    primaryKeys = ["event_id", "sponsor_id"],
    indices = [
        Index(value = ["event_id"]),
        Index(value = ["sponsor_id"])
    ],
    foreignKeys = [
        ForeignKey(entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["event_id"]),
        ForeignKey(entity = Sponsor::class,
            parentColumns = ["id"],
            childColumns = ["sponsor_id"])
    ])
data class SponsorWithEvent(
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "sponsor_id") val sponsorId: Long
)
