package org.fossasia.openevent.general.event

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

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

    @Query("SELECT * from Event WHERE id in (:ids)")
    fun getEventWithIds(ids: List<Long>): Single<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite WHERE id = :eventId")
    fun setFavorite(eventId: Long, favorite: Boolean)

    @Query("SELECT * from Event WHERE favorite = 1")
    fun getFavoriteEvents(): Flowable<List<Event>>

    @Query("SELECT id from Event WHERE favorite = 1 AND id in (:ids)")
    fun getFavoriteEventWithinIds(ids : List<Long>): Single<List<Long>>

    @Query("SELECT * from Event WHERE eventTopic = :topicId")
    fun getAllSimilarEvents(topicId: Long): Flowable<List<Event>>

}