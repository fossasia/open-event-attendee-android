package org.fossasia.openevent.general.sponsor

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SponsorWithEventDao {
    @Query("""
        SELECT sponsor.* FROM sponsor
        INNER JOIN sponsorwithevent ON
        sponsor_id = sponsorwithevent.sponsor_id
        WHERE sponsorwithevent.event_id = :eventID
        """)
    fun getSponsorWithEventId(eventID: Long): LiveData<List<Sponsor>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(join: SponsorWithEvent)
}
