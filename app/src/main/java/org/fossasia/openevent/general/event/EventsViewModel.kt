package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.RequestPasswordReset
import org.fossasia.openevent.general.auth.forgot.PasswordReset
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.paging.EventsDataSourceFactory
import org.fossasia.openevent.general.notification.NotificationService
import org.fossasia.openevent.general.search.location.SAVED_LOCATION
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

const val NEW_NOTIFICATIONS = "newNotifications"

class EventsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val notificationService: NotificationService,
    private val config: PagedList.Config
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    val mutableNewNotifications = MutableLiveData<Boolean>()
    val newNotifications: LiveData<Boolean> = mutableNewNotifications
    private val mutableProgress = MediatorLiveData<Boolean>()
    val progress: MediatorLiveData<Boolean> = mutableProgress
    private val mutableDialogProgress = MutableLiveData<Boolean>()
    val dialogProgress: LiveData<Boolean> = mutableDialogProgress
    private val mutablePagedEvents = MutableLiveData<PagedList<Event>>()
    val pagedEvents: LiveData<PagedList<Event>> = mutablePagedEvents
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    var lastSearch = ""
    private val mutableSavedLocation = MutableLiveData<String>()
    val savedLocation: LiveData<String> = mutableSavedLocation
    private lateinit var sourceFactory: EventsDataSourceFactory
    private val mutableResetPasswordEmail = MutableLiveData<String>()
    val resetPasswordEmail: LiveData<String> = mutableResetPasswordEmail

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun loadLocation() {
        mutableSavedLocation.value = preference.getString(SAVED_LOCATION)
            ?: resource.getString(R.string.enter_location)
    }

    fun loadLocationEvents() {
        if (mutableSavedLocation.value == null) return

        sourceFactory = EventsDataSourceFactory(
            compositeDisposable,
            eventService,
            mutableSavedLocation.value,
            mutableProgress
        )
        val eventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += eventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                val currentPagedEvents = mutablePagedEvents.value
                if (currentPagedEvents == null) {
                    mutablePagedEvents.value = it
                } else {
                    currentPagedEvents.addAll(it)
                    mutablePagedEvents.value = currentPagedEvents
                }
            }, {
                Timber.e(it, "Error fetching events")
                mutableMessage.value = resource.getString(R.string.error_fetching_events_message)
            })
    }
    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        mutablePagedEvents.value = null
    }

    fun clearLastSearch() {
        lastSearch = ""
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutableMessage.value = resource.getString(R.string.error)
            })
    }

    fun syncNotifications() {
        if (!isLoggedIn())
            return
        compositeDisposable += notificationService.syncNotifications(getId())
            .withDefaultSchedulers()
            .subscribe({ list ->
                list?.forEach {
                    if (!it.isRead) {
                        preference.putBoolean(NEW_NOTIFICATIONS, true)
                        mutableNewNotifications.value = true
                    }
                }
            }, {
                Timber.e(it, "Error fetching notifications")
            })
    }

    fun checkAndReset(token: String, newPassword: String) {
        val resetRequest = RequestPasswordReset(PasswordReset(token, newPassword))
        if (authHolder.isLoggedIn()) {
            compositeDisposable += authService.logout()
                .withDefaultSchedulers()
                .doOnSubscribe {
                    mutableDialogProgress.value = true
                }.subscribe {
                    resetPassword(resetRequest)
                }
        } else
            resetPassword(resetRequest)
    }

    private fun resetPassword(resetRequest: RequestPasswordReset) {
        compositeDisposable += authService.resetPassword(resetRequest)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableDialogProgress.value = true
            }.doFinally {
                mutableDialogProgress.value = false
            }.subscribe({
                Timber.e(it.toString())
                mutableMessage.value = resource.getString(R.string.reset_password_message)
                mutableResetPasswordEmail.value = it.email
            }, {
                Timber.e(it, "Failed to reset password")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
