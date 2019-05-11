package org.fossasia.openevent.general.sessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import org.fossasia.openevent.general.BuildConfig.MAPBOX_KEY
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber

class SessionViewModel(
    private val sessionService: SessionService,
    private val resource: Resource
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val mutableSession = MutableLiveData<Session>()
    val session: LiveData<Session> = mutableSession
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError

    fun loadSession(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }

        compositeDisposable.add(sessionService.fetchSession(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableSession.value = it
            }, {
                Timber.e(it, "Error fetching session id $id")
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
        )
    }

    fun loadMap(latitude: String, longitude: String): String {
        // location handling
        val BASE_URL = "https://api.mapbox.com/v4/mapbox.emerald/pin-l-marker+673ab7"
        val LOCATION = "($longitude,$latitude)/$longitude,$latitude"
        return "$BASE_URL$LOCATION,15/900x500.png?access_token=$MAPBOX_KEY"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
