package org.fossasia.openevent.general.notification

import io.reactivex.Single

class NotificationService(
    private val notificationApi: NotificationApi
) {
    fun getNotifications(userId: Long): Single<List<Notification>> {
        return notificationApi.getNotifications(userId)
    }
}
