package org.fossasia.openevent.general.attendees

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single
import org.fossasia.openevent.general.attendees.forms.CustomForm

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendees(attendees: List<Attendee>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendee(attendee: Attendee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomForms(customForms: List<CustomForm>)

    @Query("DELETE FROM Attendee")
    fun deleteAllAttendees()

    @Query("SELECT * from Attendee WHERE id in (:ids)")
    fun getAttendeesWithIds(ids: List<Long>): Single<List<Attendee>>

    @Query("SELECT * from CustomForm WHERE event = :eventId")
    fun getCustomFormsForId(eventId: Long): Single<List<CustomForm>>

    @Query("SELECT * FROM Attendee")
    fun getAllAttendees(): Single<List<Attendee>>
}
