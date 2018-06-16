package org.fossasia.openevent.general.aboutEvent

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AboutEventViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progressAboutEvent = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()
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
                    progressAboutEvent.value = true
                }).doFinally({
                    progressAboutEvent.value = false
                }).subscribe({eventList ->
                    event.value = eventList
                }, {
                    error.value = "Error fetching event"
                    Timber.e(it, "Error fetching event %d",id)
                }))
    }

    fun getAboutEventFormattedDate(date: String): String{
        val dateString = EventUtils.getLocalizedDateTime(date)
        //Format Month
        val month = dateString.dayOfWeek
        val lowerCaseMonth = month.toString().toLowerCase()
        val formatMonth = (lowerCaseMonth.substring(0, 1).toUpperCase() + lowerCaseMonth.substring(1)).substring(0, 3)
        //Format Day
        val day = dateString.month
        val lowerCaseDay = day.toString().toLowerCase()
        val formatDay = (lowerCaseDay.substring(0, 1).toUpperCase() + lowerCaseDay.substring(1)).substring(0, 3)

        return formatMonth + ", " + formatDay + " " + dateString.dayOfMonth
    }

    fun getAboutEventFormattedTime(date: String): String{
        var testDate =  Date()
        val dateString = date.replace("T"," ")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            testDate = dateFormat.parse(dateString)
        } catch (e: ParseException) {
            Timber.e(e,"Error Parsing Date")
        }
        val dateFormatFinal = SimpleDateFormat("hh:mm a Z", Locale.getDefault())
        return dateFormatFinal.format(testDate)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}