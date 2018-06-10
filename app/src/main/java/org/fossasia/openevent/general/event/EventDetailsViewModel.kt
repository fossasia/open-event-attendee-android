package org.fossasia.openevent.general.event

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EventDetailsViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()
    val mapUrl = MutableLiveData<String>()
    val mapIntentData = MutableLiveData<Intent>()
    val error = MutableLiveData<String>()

    fun loadEvent(id : Long) {
        if (id.equals(-1)) {
            error.value = "Error fetching event"
            return
        }
        compositeDisposable.add(eventService.getEvent(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally({
                    progress.value = false
                }).subscribe({
                    event.value = it
                }, {
                    Timber.e(it, "Error fetching event %d",id)
                    error.value = "Error fetching event"
                }))
    }

    fun loadMap(event: Event){
        //location handling
        val mapUrlInitial = "https://maps.googleapis.com/maps/api/staticmap?center="
        val mapUrlProperties = "&zoom=12&size=1200x390&markers=color:red%7C"
        val mapUrlMapType = "&markers=size:mid&maptype=roadmap"

        val latLong: String = "" +event.latitude + "," + event.longitude

        mapUrl.value = mapUrlInitial + latLong + mapUrlProperties + latLong + mapUrlMapType
    }

    fun loadMapIntent(event: Event, context: Context?){
        val gmmIntentUri = Uri.parse("geo:<"+event.latitude+">,<"+event.longitude+">?q=<"+event.latitude+">,<"+event.longitude+">")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        if (mapIntent.resolveActivity(context?.packageManager) != null) {
            mapIntentData.value = mapIntent
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}