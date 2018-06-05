package org.fossasia.openevent.general.event

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface TicketsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTickets(tickets: List<Ticket>)

    @Query("SELECT * FROM Ticket")
    fun getAllTickets(): Single<List<Ticket>>

    @Query("DELETE FROM Ticket")
    fun deleteAll()
}