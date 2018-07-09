package org.fossasia.openevent.general.ticket

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TicketsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTickets(tickets: List<Ticket>)

    @Query("DELETE FROM Ticket")
    fun deleteAll()

    @Query("SELECT * from Ticket WHERE event = :eventId")
    fun getTicketsForEvent(eventId: Long): Flowable<List<Ticket>>

    @Query("SELECT * from Ticket WHERE id = :id")
    fun getTicketDetails(id: Long): Single<Ticket>

    @Query("SELECT price from Ticket WHERE id in (:ids)")
    fun getTicketPriceWithIds(ids : List<Int>): Single<List<String>>
}
