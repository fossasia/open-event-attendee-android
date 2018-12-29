package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.User
import timber.log.Timber

class EventDetailsViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableError = MutableLiveData<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent

    fun loadEvent(id: Long) {
        if (id.equals(-1)) {
            mutableError.value = "Error fetching event"
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
                mutableError.value = "Error fetching event"
            })
        )
    }

    fun loadMap(event: Event): String {
        // location handling
        val mapUrlInitial = "https://maps.googleapis.com/maps/api/staticmap?center="
        val mapUrlProperties = "&zoom=12&size=1200x390&markers=color:red%7C"
        val mapUrlMapType = "&markers=size:mid&maptype=roadmap"

        val latLong: String = "" + event.latitude + "," + event.longitude

        return mapUrlInitial + latLong + mapUrlProperties + latLong + mapUrlMapType
    }

    fun setFavorite(eventId: Long, favourite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favourite)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutableError.value = "Error"
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
