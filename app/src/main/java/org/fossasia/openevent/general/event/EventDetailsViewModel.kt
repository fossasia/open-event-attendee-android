package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.BuildConfig.MAPBOX_KEY
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserId
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.feedback.Feedback
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.sponsor.Sponsor
import org.fossasia.openevent.general.sponsor.SponsorService
import timber.log.Timber

class EventDetailsViewModel(
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val speakerService: SpeakerService,
    private val sponsorService: SponsorService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableEventFeedback = MutableLiveData<List<Feedback>>()
    val eventFeedback: LiveData<List<Feedback>> = mutableEventFeedback
    private val mutableEventSessions = MutableLiveData<List<Session>>()
    val eventSessions: LiveData<List<Session>> = mutableEventSessions
    var eventSpeakers: LiveData<List<Speaker>> = MutableLiveData()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun loadEventFeedback(id: Long) {
        compositeDisposable.add(eventService.getEventFeedback(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableEventFeedback.value = it
            }, {
                Timber.e(it, "Error fetching events feedback")
            })
        )
    }

    fun submitFeedback(comment: String, rating: Float?, eventId: Long) {
        val feedback = Feedback(rating = rating.toString(), comment = comment,
            event = EventId(eventId), user = UserId(getId()))
        compositeDisposable.add(eventService.submitFeedback(feedback)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //Do Nothing
            }, {
                it.message.toString() == "HTTP 400 BAD REQUEST"
            })
        )
    }
    fun fetchEventSpeakers(id: Long) {
        speakerService.fetchSpeakersForEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                //Do Nothing
            }, {
                Timber.e(it, "Error fetching speaker for event id %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadEventSpeakers(id: Long): LiveData<List<Speaker>> {
        eventSpeakers = speakerService.fetchSpeakersFromDb(id)
        return eventSpeakers
    }

    fun fetchEventSponsors(id: Long) {
        sponsorService.fetchSponsorsWithEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                //Do Nothing
            }, {
                Timber.e(it, "Error fetching sponsor for event id %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadEventSponsors(id: Long): LiveData<List<Sponsor>> = sponsorService.fetchSponsorsFromDb(id)

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable.add(eventService.getEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
        )
    }

    fun loadEventSessions(id: Long) {
        compositeDisposable.add(eventService.getEventSessions(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableEventSessions.value = it
            }, {
                Timber.e(it, "Error fetching events sessions")
            })
        )
    }

    fun loadMap(event: Event): String {
        // location handling
        val BASE_URL = "https://api.mapbox.com/v4/mapbox.emerald/pin-l-marker+673ab7"
        val LOCATION = "(" + event.longitude + "," + event.latitude + ")/" + event.longitude + "," + event.latitude
        return BASE_URL + LOCATION + ",15/900x500.png?access_token=" + MAPBOX_KEY
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favorite)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutableError.value = resource.getString(R.string.error)
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
