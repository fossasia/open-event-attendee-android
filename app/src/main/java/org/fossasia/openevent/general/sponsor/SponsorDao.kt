package org.fossasia.openevent.general.sponsor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface SponsorDao {

    @Insert(onConflict = REPLACE)
    fun insertSponsor(sponsor: Sponsor)
}
