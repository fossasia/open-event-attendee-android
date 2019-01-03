package org.fossasia.openevent.general.favorite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.search.SearchTimeViewModel
import timber.log.Timber
import java.util.Calendar

class FavouriteEventsViewModel(private val eventService: EventService) : ViewModel() {

    private val TO_SEARCH: String = "ToSearchFragment"
    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = MutableLiveData<String>()
    val error: LiveData<String> = mutableError
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents

    fun loadFavoriteEvents() {
        compositeDisposable.add(
            eventService.getFavoriteEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mutableEvents.value = it
                    mutableProgress.value = false
                }, {
                    Timber.e(it, "Error fetching favorite events")
                    mutableError.value = "Error fetching favorite events"
                })
        )
    }

    fun setFavorite(eventId: Long, favourite: Boolean) {
        compositeDisposable.add(
            eventService.setFavorite(eventId, favourite)
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

    fun searchToday(calendar: Calendar, searchTimeViewModel: SearchTimeViewModel, activity: AppCompatActivity?) {
        searchTimeViewModel.saveDate(EventUtils.getSimpleFormattedDate(calendar.time))
        calendar.add(Calendar.DATE, 1)
        searchTimeViewModel.saveNextDate(EventUtils.getSimpleFormattedDate(calendar.time))
        searchTimeViewModel.saveDate(TODAY)
        redirectToSearch(activity)
    }

    fun searchTomorrow(calendar: Calendar, searchTimeViewModel: SearchTimeViewModel, activity: AppCompatActivity?) {
        calendar.add(Calendar.DATE, 1)
        searchTimeViewModel.saveNextDate(EventUtils.getSimpleFormattedDate(calendar.time))
        calendar.add(Calendar.DATE, 1)
        searchTimeViewModel.saveNextToNextDate(EventUtils.getSimpleFormattedDate(calendar.time))
        searchTimeViewModel.saveDate(TOMORROW)
        redirectToSearch(activity)
    }

    fun searchWeekend(calendar: Calendar, searchTimeViewModel: SearchTimeViewModel, activity: AppCompatActivity?) {
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        if (today != Calendar.SATURDAY) {
            val offset = Calendar.SATURDAY - today
            calendar.add(Calendar.DATE, offset)
        }
        searchTimeViewModel.saveWeekendDate(EventUtils.getSimpleFormattedDate(calendar.time))
        calendar.add(Calendar.DATE, 1)
        searchTimeViewModel.saveNextToWeekendDate(EventUtils.getSimpleFormattedDate(calendar.time))
        searchTimeViewModel.saveDate(THIS_WEEKEND)
        redirectToSearch(activity)
    }

    fun searchNextMonth(calendar: Calendar, searchTimeViewModel: SearchTimeViewModel, activity: AppCompatActivity?) {
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val offset = 30 - today
        calendar.add(Calendar.DATE, offset)
        searchTimeViewModel.saveNextMonth(EventUtils.getSimpleFormattedDate(calendar.time))
        calendar.add(Calendar.MONTH, 1)
        searchTimeViewModel.saveNextToNextMonth(EventUtils.getSimpleFormattedDate(calendar.time))
        searchTimeViewModel.saveDate(NEXT_MONTH)
        redirectToSearch(activity)
    }

    private fun redirectToSearch(activity: AppCompatActivity?) {
        val intent = Intent(activity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val bundle = Bundle()
        bundle.putBoolean(TO_SEARCH, true)
        intent.putExtras(bundle)
        activity?.startActivity(intent)
    }
}
