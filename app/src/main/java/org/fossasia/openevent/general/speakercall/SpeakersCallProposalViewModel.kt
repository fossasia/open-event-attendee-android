package org.fossasia.openevent.general.speakercall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SpeakersCallProposalViewModel(
    private val resource: Resource,
    private val speakerService: SpeakerService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableSpeakerProgress = MutableLiveData<Boolean>(false)
    val speakerProgress: LiveData<Boolean> = mutableSpeakerProgress
    private val mutableSpeaker = MutableLiveData<Speaker>()
    val speaker: LiveData<Speaker> = mutableSpeaker

    var isSpeakerInfoShown = true

    fun loadSpeaker(speakerId: Long) {
        if (speakerId == -1L) {
            return
        }

        compositeDisposable += speakerService.fetchSpeaker(speakerId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableSpeakerProgress.value = true
            }.subscribe({
                mutableSpeaker.value = it
                mutableSpeakerProgress.value = false
            }, {
                Timber.e(it, "Fail on fetching speaker id $speakerId")
                mutableSpeakerProgress.value = false
            })
    }
}
