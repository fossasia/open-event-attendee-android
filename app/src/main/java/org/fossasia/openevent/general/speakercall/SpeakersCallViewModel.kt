package org.fossasia.openevent.general.speakercall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SpeakersCallViewModel(
    private val eventService: EventService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableSpeakersCall = MutableLiveData<SpeakersCall>()
    val speakersCall: LiveData<SpeakersCall> = mutableSpeakersCall
    private val mutableError = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = mutableError
    private val mutableProgress = MutableLiveData<Boolean>(true)
    val progress: LiveData<Boolean> = mutableProgress

    fun loadSpeakersCall(eventId: Long) {
        if (eventId == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_event_section_message,
                resource.getString(R.string.speakers_call))
            mutableProgress.value = false
            return
        }

        compositeDisposable += eventService.getSpeakerCall(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableSpeakersCall.value = it
            }, {
                mutableError.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.speakers_call))
                Timber.e(it, "Error fetching speakers call for event $eventId")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
