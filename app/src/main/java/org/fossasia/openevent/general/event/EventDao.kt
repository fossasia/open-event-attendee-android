package org.fossasia.openevent.general.event

import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

interface EventDao {
    @Insert
    fun insertEvent(event: Event)

    @Query("DELETE FROM event_table")
    fun deleteAll()

    @Query("SELECT * from event_table ORDER BY event ASC")
    fun getAllEvents(): List<Event>
}