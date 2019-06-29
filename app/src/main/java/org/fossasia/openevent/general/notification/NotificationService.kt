package org.fossasia.openevent.general.notification

import io.reactivex.Single

class NotificationService(
    private val notificationApi: NotificationApi,
    private val notificationDao: NotificationDao
) {

    fun getNotifications(userId: Long): Single<List<Notification>> {
        return notificationDao.getNotifications()
            .onErrorResumeNext {
                notificationApi.getNotifications(userId).map {
                    notificationDao.insertNotifications(it)
                    it
                }
            }
    }

    fun syncNotifications(userId: Long): Single<List<Notification>> {
        return notificationApi.getNotifications(userId).map {
            notificationDao.insertNotifications(it)
            it
        }
    }

    fun updateNotification(notification: Notification): Single<Notification> {
        return notificationApi.updateNotification(notification.id, notification).map {
            notificationDao.insertNotification(it)
            it
        }
    }
}
