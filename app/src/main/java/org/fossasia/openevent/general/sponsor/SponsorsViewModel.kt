package org.fossasia.openevent.general.sponsor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber

class SponsorsViewModel(
    private val sponsorService: SponsorService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableSponsors = MutableLiveData<List<Sponsor>>()
    val sponsors: LiveData<List<Sponsor>> = mutableSponsors
    private val mutableError = MutableLiveData<String>()
    val error: LiveData<String> = mutableError
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress

    fun loadSponsors(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }

        compositeDisposable += sponsorService.fetchSponsorsWithEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableSponsors.value = it
                mutableProgress.value = false
            }, {
                Timber.e("Error fetching speaker for id $id")
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
