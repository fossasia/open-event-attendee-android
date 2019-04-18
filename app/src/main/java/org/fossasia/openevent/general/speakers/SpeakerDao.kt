package org.fossasia.openevent.general.speakers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface SpeakerDao {

    @Insert(onConflict = REPLACE)
    fun insertSpeakers(speakers: List<Speaker>)

    @Insert(onConflict = REPLACE)
    fun insertSpeaker(speaker: Speaker)
}
