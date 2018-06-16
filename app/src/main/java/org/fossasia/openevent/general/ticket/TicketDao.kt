package org.fossasia.openevent.general.ticket

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface TicketsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTickets(tickets: List<Ticket>)

    @Query("DELETE FROM Ticket")
    fun deleteAll()

    @Query("SELECT * from Ticket WHERE event = :eventId")
    fun getTicketsForEvent(eventId: Long): Flowable<List<Ticket>>
}
