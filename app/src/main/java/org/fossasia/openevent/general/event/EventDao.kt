package org.fossasia.openevent.general.event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.fossasia.openevent.general.event.topic.EventTopic

@Dao
interface EventDao {
    @Insert(onConflict = REPLACE)
    fun insertEvents(events: List<Event>)

    @Insert(onConflict = REPLACE)
    fun insertEvent(event: Event)

    @Query("DELETE FROM Event")
    fun deleteAll()

    @Query("SELECT * from Event ORDER BY startsAt DESC")
    fun getAllEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE id = :id")
    fun getEvent(id: Long): Flowable<Event>

    @Query("SELECT * FROM event WHERE id = :eventId")
    fun getEventById(eventId: Long): Single<Event>

    @Query("SELECT * from Event WHERE id in (:ids)")
    fun getEventWithIds(ids: List<Long>): Flowable<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite WHERE id = :eventId")
    fun setFavorite(eventId: Long, favorite: Boolean)

    @Query("SELECT * from Event WHERE favorite = 1")
    fun getFavoriteEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE favorite = 1 AND id in (:ids)")
    fun getFavoriteEventWithinIds(ids: List<Long>): Single<List<Event>>

    @Query("SELECT * from Event WHERE eventTopic = :eventTopic")
    fun getAllSimilarEvents(eventTopic: EventTopic): Flowable<List<Event>>

    @Query("SELECT * FROM EventTopic WHERE id=:topicId")
    fun getEventTopic(topicId: Long): Single<EventTopic>

    @Query("UPDATE Event SET favorite = :favorite AND favoriteEventId = NULL")
    fun clearFavoriteEvents(favorite: Boolean = false)
}
