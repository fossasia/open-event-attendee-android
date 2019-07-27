package org.fossasia.openevent.general.ticket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTickets(tickets: List<Ticket>)

    @Query("DELETE FROM Ticket")
    fun deleteAll()

    @Query("SELECT * from Ticket WHERE event = :eventId")
    fun getTicketsForEvent(eventId: Long): Flowable<List<Ticket>>

    @Query("SELECT * from Ticket WHERE id = :id")
    fun getTicketDetails(id: Long): Single<Ticket>

    @Query("SELECT * from Ticket WHERE id in (:ids)")
    fun getTicketsWithIds(ids: List<Int>): Single<List<Ticket>>

    @Query("SELECT MAX(price) as maxValue, MIN(price) as minValue from Ticket WHERE event = :eventId")
    fun getTicketPriceRange(eventId: Long): Single<TicketPriceRange>
}
