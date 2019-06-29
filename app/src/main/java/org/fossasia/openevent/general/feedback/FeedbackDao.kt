package org.fossasia.openevent.general.feedback

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Single

@Dao
interface FeedbackDao {

    @Insert(onConflict = REPLACE)
    fun insertFeedback(feedbacks: List<Feedback>)

    @Insert(onConflict = REPLACE)
    fun insertSingleFeedback(feedback: Feedback)

    @Query("SELECT * FROM feedback WHERE event = :eventId")
    fun getAllFeedbackUnderEvent(eventId: Long): Single<List<Feedback>>
}
