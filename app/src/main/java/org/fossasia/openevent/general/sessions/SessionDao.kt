package org.fossasia.openevent.general.sessions

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface SessionDao {

    @Insert(onConflict = REPLACE)
    fun insertSessions(sessions: List<Session>)

    @Insert(onConflict = REPLACE)
    fun insertSession(session: Session)

    @Query("SELECT * FROM Session WHERE id =:id")
    fun getSessionById(id: Long): Flowable<Session>

    @Query("SELECT * FROM Session")
    fun getAllSessions(): LiveData<List<Session>>

    @Query("DELETE FROM Session")
    fun deleteCurrentSessions()
}
