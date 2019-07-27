package org.fossasia.openevent.general.speakercall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.SessionService
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SpeakersCallViewModel(
    private val eventService: EventService,
    private val resource: Resource,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val speakerService: SpeakerService,
    private val sessionService: SessionService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableSpeakersCall = MutableLiveData<SpeakersCall>()
    val speakersCall: LiveData<SpeakersCall> = mutableSpeakersCall
    private val mutableSessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> = mutableSessions
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableProgress = MutableLiveData(true)
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSpeaker = MutableLiveData<Speaker>()
    val speaker: LiveData<Speaker> = mutableSpeaker
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableEmptySpeakersCall = MutableLiveData<Boolean>()
    val emptySpeakersCall: LiveData<Boolean> = mutableEmptySpeakersCall

    fun isLoggedIn(): Boolean = authHolder.isLoggedIn()

    fun loadMyUserAndSpeaker(eventId: Long, eventIdentifier: String) {
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                mutableUser.value = user
                loadMySpeaker(user, eventId, eventIdentifier)
            }) {
                Timber.e(it, "Failure")
                mutableMessage.value = resource.getString(R.string.failure)
            }
    }

    fun loadMySpeaker(user: User, eventId: Long, eventIdentifier: String) {
        val query = """[{
                |   'and':[{
                |       'name':'event',
                |       'op':'has',
                |       'val': {
                |           'name': 'identifier',
                |           'op': 'eq',
                |           'val': '$eventIdentifier'
                |       }
                |    }, {
                |       'name':'email',
                |       'op':'eq',
                |       'val':'${user.email}'
                |    }]
                |}]""".trimMargin().replace("'", "\"")

        compositeDisposable += speakerService.getSpeakerProfileOfEmailAndEvent(user, eventId, query)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableMessage.value = resource.getString(R.string.loading_speaker_profile_message)
            }.subscribe({
                mutableSpeaker.value = it
                loadMySessions(it.id, eventIdentifier)
            }, {
                mutableMessage.value = resource.getString(R.string.no_speaker_profile_created_message)
            })
    }

    private fun loadMySessions(speakerId: Long, eventIdentifier: String) {
        val query = """[{
                 |   'and':[{
                |       'name':'event',
                |       'op':'has',
                |       'val': {
                |           'name': 'identifier',
                |           'op': 'eq',
                |           'val': '$eventIdentifier'
                |       }
                |    }]
                |}]""".trimMargin().replace("'", "\"")

        compositeDisposable += sessionService.getSessionsUnderSpeakerAndEvent(speakerId, query)
            .withDefaultSchedulers()
            .subscribe({
                mutableSessions.value = it
            }, {
                mutableMessage.value = resource.getString(R.string.no_sessions_created_message)
                Timber.e(it, "Fail on loading session of this user")
            })
    }

    fun loadSpeakersCall(eventId: Long) {
        if (eventId == -1L) {
            mutableMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                resource.getString(R.string.speakers_call))
            mutableEmptySpeakersCall.value = true
            mutableProgress.value = false
            return
        }

        compositeDisposable += eventService.getSpeakerCall(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableSpeakersCall.value = it
                mutableEmptySpeakersCall.value = false
            }, {
                mutableMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.speakers_call))
                mutableEmptySpeakersCall.value = true
                Timber.e(it, "Error fetching speakers call for event $eventId")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
