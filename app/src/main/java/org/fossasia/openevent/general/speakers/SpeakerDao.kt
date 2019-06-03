package org.fossasia.openevent.general.speakers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface SpeakerDao {

    @Insert(onConflict = REPLACE)
    fun insertSpeakers(speakers: List<Speaker>)

    @Insert(onConflict = REPLACE)
    fun insertSpeaker(speaker: Speaker)

    @Query("SELECT * from Speaker WHERE id = :id")
    fun getSpeaker(id: Long): Flowable<Speaker>

    @Query("SELECT * FROM speaker WHERE email = :email AND event = :eventId")
    fun getSpeakerByEmailAndEvent(email: String, eventId: Long): Single<Speaker>
}
