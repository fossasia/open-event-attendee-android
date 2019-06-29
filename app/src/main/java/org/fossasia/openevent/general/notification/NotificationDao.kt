package org.fossasia.openevent.general.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Single

@Dao
interface NotificationDao {
    @Insert(onConflict = REPLACE)
    fun insertNotifications(notifications: List<Notification>)

    @Insert(onConflict = REPLACE)
    fun insertNotification(notification: Notification)

    @Query("SELECT * FROM Notification")
    fun getNotifications(): Single<List<Notification>>
}
