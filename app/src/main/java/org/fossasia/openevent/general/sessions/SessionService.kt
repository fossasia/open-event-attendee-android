package org.fossasia.openevent.general.sessions

import io.reactivex.Flowable
import io.reactivex.Single

class SessionService(
    private val sessionApi: SessionApi,
    private val sessionDao: SessionDao
) {
    fun fetchSessionForEvent(id: Long): Single<List<Session>> {
        return sessionApi.getSessionsForEvent(id)
            .doOnSuccess { sessions ->
                sessionDao.deleteCurrentSessions()
                sessionDao.insertSessions(sessions)
            }
        }

    fun fetchSession(id: Long): Flowable<Session> {
        return sessionDao.getSessionById(id)
    }
}
