package org.fossasia.openevent.general.sessions

import io.reactivex.Single
import org.fossasia.openevent.general.speakercall.Proposal

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

    fun fetchSession(id: Long): Single<Session> =
        sessionApi.getSessionById(id)

    fun createSession(proposal: Proposal): Single<Session> =
        sessionApi.createSession(proposal).doOnSuccess {
            sessionDao.insertSession(it)
        }

    fun updateSession(sessionId: Long, proposal: Proposal): Single<Session> =
        sessionApi.updateSession(sessionId, proposal).doOnSuccess {
            sessionDao.insertSession(it)
        }

    fun getSessionsUnderSpeakerAndEvent(speakerId: Long, query: String): Single<List<Session>> =
        sessionApi.getSessionsUnderSpeaker(speakerId, filter = query)
}
