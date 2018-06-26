package org.fossasia.openevent.general.attendees

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendees(attendees: List<Attendee>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendee(attendees: Attendee)

    @Query("DELETE FROM Attendee")
    fun deleteAll()
}