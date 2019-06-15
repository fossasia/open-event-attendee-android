package org.fossasia.openevent.general.speakers

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SpeakerWithEventDao {
    @Query("""
        SELECT speaker.* FROM speaker
        INNER JOIN speakerwithevent ON
        speaker.id = speakerwithevent.speaker_id
        WHERE speakerwithevent.event_id = :eventID
        """)
    fun getSpeakerWithEventId(eventID: Long): LiveData<List<Speaker>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(join: SpeakerWithEvent)
}
