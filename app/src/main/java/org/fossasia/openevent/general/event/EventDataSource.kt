package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.ItemKeyedDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference

class EventDataSource(
        private val eventService: EventService,
        private val preference: Preference)
    : ItemKeyedDataSource<Long, Event>() {
    private val tokenKey = "LOCATION"
    val savedLocation by lazy { preference.getString(tokenKey) }
    val compositeDisposable = CompositeDisposable()
    val progress = MutableLiveData<Boolean>()

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Event>) {
        preference.putString(tokenKey, savedLocation)
        val query = "[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$savedLocation%\"}]"

        compositeDisposable.add(eventService.getEventsByLocation(query,1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{
                    progress.value = true
                }
                .doFinally{
                    progress.value = false
                }
                .subscribe(
                        {
                            callback.onResult(it)
                        }, {}))
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Event>) {
        preference.putString(tokenKey, savedLocation)
        val query = "[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$savedLocation%\"}]"

        compositeDisposable.add(eventService.getEventsByLocation(query,2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            callback.onResult(it)
                        }, {}))
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Event>) {
        // ignored, since we only ever append to our initial load
    }

    override fun getKey(item: Event): Long {
        return item.id
    }

}