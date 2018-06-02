package org.fossasia.openevent.general.event

import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Single

interface EventDao {
    @Insert(onConflict = REPLACE)
    fun insertEvent(event: Event)

    @Query("DELETE FROM Event")
    fun deleteAll()

    @Query("SELECT * from Event ORDER BY startsAt ASC")
    fun getAllEvents(): Single<List<Event>>
}