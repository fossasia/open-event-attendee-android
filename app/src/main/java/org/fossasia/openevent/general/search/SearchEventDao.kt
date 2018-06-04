package org.fossasia.openevent.general.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.fossasia.openevent.general.event.Event

@Dao
interface SearchEventDao {
    @Insert(onConflict = REPLACE)
    fun insertEvents(events: List<Event>)

    @Insert(onConflict = REPLACE)
    fun insertEvent(event: Event)

    @Query("DELETE FROM Event")
    fun deleteAll()

    @Query("SELECT * from Event ORDER BY startsAt DESC")
    fun getAllEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE id = :id")
    fun getEvent(id: Long): Flowable<Event>
}