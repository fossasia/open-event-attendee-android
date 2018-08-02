package org.fossasia.openevent.general.attendees

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendees(attendees: List<Attendee>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendee(attendee: Attendee)

    @Query("DELETE FROM Attendee")
    fun deleteAll()

    @Query("SELECT * from Attendee WHERE id in (:ids)")
    fun getAttendeesWithIds(ids: List<Long>): Single<List<Attendee>>
}