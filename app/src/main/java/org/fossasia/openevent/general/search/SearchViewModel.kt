package org.fossasia.openevent.general.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.TextUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class SearchViewModel(private val eventService: EventService, private val preference: Preference, private val network: Network) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val tokenKey = "LOCATION"
    private val tokenKeyDate = "DATE"
    private val tokenKeyNextDate = "NEXT_DATE"
    private val tokenKeyNextToNextDate = "NEXT_TO_NEXT_DATE"
    private val tokenKeyWeekendDate = "WEEKEND"
    private val tokenKeyWeekendNextDate = "WEEKEND_NEXT_DATE"
    private val tokenKeyNextMonth = "NEXT_MONTH"
    private val tokenKeyNextToNextMonth = "NEXT_TO_NEXT_MONTH"

    val showShimmerResults = MutableLiveData<Boolean>()
    val events = MutableLiveData<List<Event>>()
    val error = MutableLiveData<String>()
    val showNoInternetError = MutableLiveData<Boolean>()
    var searchEvent: String? = null
    val savedLocation by lazy { preference.getString(tokenKey) }
    val savedDate by lazy { preference.getString(tokenKeyDate) }
    val savedNextDate by lazy { preference.getString(tokenKeyNextDate) }
    val savedNextToNextDate by lazy { preference.getString(tokenKeyNextToNextDate) }
    val savedWeekendDate by lazy { preference.getString(tokenKeyWeekendDate) }
    val savedWeekendNextDate by lazy { preference.getString(tokenKeyWeekendNextDate) }
    val savedNextMonth by lazy { preference.getString(tokenKeyNextMonth) }
    val savedNextToNextMonth by lazy { preference.getString(tokenKeyNextToNextMonth) }

    fun loadEvents(location: String, time: String) {
        if (!isConnected()) return
        preference.putString(tokenKey, location)
        val query: String = if (TextUtils.isEmpty(location))
            "[{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"}]"
        else if (time == "Anytime")
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"}]}]"
        else if (time=="Today")
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"},{\"name\":\"starts-at\",\"op\":\"ge\",\"val\":\"$savedDate%\"},{\"name\":\"starts-at\",\"op\":\"lt\",\"val\":\"$savedNextDate%\"}]}]"
        else if (time == "Tomorrow")
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"},{\"name\":\"starts-at\",\"op\":\"ge\",\"val\":\"$savedNextDate%\"},{\"name\":\"starts-at\",\"op\":\"lt\",\"val\":\"$savedNextToNextDate%\"}]}]"
        else if (time == "This Weekend")
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"},{\"name\":\"starts-at\",\"op\":\"ge\",\"val\":\"$savedWeekendDate%\"},{\"name\":\"starts-at\",\"op\":\"lt\",\"val\":\"$savedWeekendNextDate%\"}]}]"
        else if (time == "In the next month")
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"},{\"name\":\"starts-at\",\"op\":\"ge\",\"val\":\"$savedNextMonth%\"},{\"name\":\"starts-at\",\"op\":\"lt\",\"val\":\"$savedNextToNextMonth%\"}]}]"
        else
            "[{\"and\":[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$location%\"},{\"name\":\"name\",\"op\":\"ilike\",\"val\":\"%$searchEvent%\"},{\"name\":\"starts-at\",\"op\":\"ge\",\"val\":\"$savedDate%\"},{\"name\":\"starts-at\",\"op\":\"lt\",\"val\":\"$savedNextDate%\"}]}]"

        compositeDisposable.add(eventService.getSearchEvents(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showShimmerResults.value = true
                }.doFinally {
                    showShimmerResults.value = false
                }.subscribe({
                    events.value = it
                }, {
                    Timber.e(it, "Error fetching events")
                    error.value = "Error fetching events"
                }))

        preference.remove(tokenKeyDate)
        preference.remove(tokenKeyNextDate)
    }

    fun setFavorite(eventId: Long, favourite: Boolean) {
        compositeDisposable.add(eventService.setFavorite(eventId, favourite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Successfully added %d to favorites", eventId)
                }, {
                    Timber.e(it, "Error adding %d to favorites", eventId)
                    error.value = "Error adding to favorites"
                }))
    }

    fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        showNoInternetError.value = !isConnected
        showShimmerResults.value = isConnected
        return isConnected
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
