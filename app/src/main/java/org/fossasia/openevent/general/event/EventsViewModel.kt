package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.notification.Notification
import org.fossasia.openevent.general.notification.NotificationService
import org.fossasia.openevent.general.search.location.SAVED_LOCATION
import org.fossasia.openevent.general.favorite.FavoriteEvent
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

const val NEW_NOTIFICATIONS = "newNotifications"

class EventsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val authHolder: AuthHolder,
    private val notificationService: NotificationService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableShowShimmerEvents = MutableLiveData<Boolean>()
    val showShimmerEvents: LiveData<Boolean> = mutableShowShimmerEvents
    var lastSearch = ""
    private val mutableSavedLocation = MutableLiveData<String>()
    val savedLocation: LiveData<String> = mutableSavedLocation
    private var oldNotifications: List<Notification>? = null

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun loadLocation() {
        mutableSavedLocation.value = preference.getString(SAVED_LOCATION)
            ?: resource.getString(R.string.enter_location)
    }

    fun loadLocationEvents() {
        if (mutableSavedLocation.value == null) return

        if (lastSearch != savedLocation.value) {
            compositeDisposable += eventService.getEventsByLocation(mutableSavedLocation.value)
                .withDefaultSchedulers()
                .distinctUntilChanged()
                .doOnSubscribe {
                    mutableShowShimmerEvents.value = true
                }
                .doFinally {
                    stopLoaders()
                }.subscribe({
                    stopLoaders()
                    mutableEvents.value = it
                }, {
                    stopLoaders()
                    Timber.e(it, "Error fetching events")
                    mutableMessage.value = resource.getString(R.string.error_fetching_events_message)
                })
        } else {
            mutableProgress.value = false
        }
    }

    private fun stopLoaders() {
        mutableProgress.value = false
        mutableShowShimmerEvents.value = false
        lastSearch = mutableSavedLocation.value ?: ""
    }
    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        mutableEvents.value = null
    }

    fun clearLastSearch() {
        lastSearch = ""
    }

    fun setFavorite(event: Event, favorite: Boolean) {
        if (favorite) {
            addFavorite(event)
        } else {
            removeFavorite(event)
        }
    }

    private fun addFavorite(event: Event) {
        val favoriteEvent = FavoriteEvent(authHolder.getId(), EventId(event.id))
        compositeDisposable += eventService.addFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutableMessage.value = resource.getString(R.string.add_event_to_shortlist_message)
            }, {
                mutableMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on adding like for event ID ${event.id}")
            })
    }

    private fun removeFavorite(event: Event) {
        Timber.d("DEBUGGING Removing favorites in EventsFragment 1")
        val favoriteEventId = event.favoriteEventId ?: return

        Timber.d("DEBUGGING Removing favorites in EventsFragment 2")
        val favoriteEvent = FavoriteEvent(favoriteEventId, EventId(event.id))
        compositeDisposable += eventService.removeFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("DEBUGGING Removing favorites in EventsFragment SUCCESS")
                mutableMessage.value = resource.getString(R.string.remove_event_from_shortlist_message)
            }, {
                Timber.d("DEBUGGING Removing favorites in EventsFragment FAIL")
                mutableMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on removing like for event ID ${event.id}")
            })
    }

    fun getNotifications() {
        if (!isLoggedIn())
            return

        compositeDisposable += notificationService.getNotifications(getId())
            .withDefaultSchedulers()
            .subscribe({ list ->
                oldNotifications = list
                syncNotifications()
            }, {
                Timber.e(it, "Error fetching notifications")
            })
    }

    private fun syncNotifications() {
        compositeDisposable += notificationService.syncNotifications(getId())
            .withDefaultSchedulers()
            .subscribe({ list ->
                list?.let { checkNewNotifications(it) }
            }, {
                Timber.e(it, "Error fetching notifications")
            })
    }

    private fun checkNewNotifications(newNotifications: List<Notification>) {
        if (newNotifications.size != oldNotifications?.size) {
            preference.putBoolean(NEW_NOTIFICATIONS, true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
