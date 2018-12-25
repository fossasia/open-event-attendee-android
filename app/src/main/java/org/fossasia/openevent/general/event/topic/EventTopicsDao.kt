package org.fossasia.openevent.general.event.topic

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
