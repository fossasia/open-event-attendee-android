package org.fossasia.openevent.general.notification

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationApi {

    @GET("users/{userId}/notifications?sort=received-at")
    fun getNotifications(@Path("userId") userId: Long): Single<List<Notification>>

    @PATCH("notifications/{notification_id}")
    fun updateNotification(
        @Path("notification_id") notificationId: Int,
        @Body notification: Notification
    ): Single<Notification>

    @DELETE("notifications/{notification_id}")
    fun deleteNotification(@Path("notification_id") notificationId: Long): Completable
}
