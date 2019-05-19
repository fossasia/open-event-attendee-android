package org.fossasia.openevent.general.speakercall

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Single

@Dao
interface SpeakersCallDao {

    @Insert(onConflict = REPLACE)
    fun insertSpeakerCall(speakers: SpeakersCall)

    @Query("SELECT * from SpeakersCall WHERE id = :id")
    fun getSpeakerCall(id: Long): Single<SpeakersCall>
}
