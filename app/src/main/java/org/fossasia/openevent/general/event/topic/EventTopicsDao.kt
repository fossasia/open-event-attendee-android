package org.fossasia.openevent.general.event.topic

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface EventTopicsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventTopics(eventTopic: List<EventTopic?>)

    @Query("SELECT * from EventTopic")
    fun getAllEventTopics(): Flowable<List<EventTopic>>

    @Query("DELETE FROM EventTopic")
    fun deleteAll()

}