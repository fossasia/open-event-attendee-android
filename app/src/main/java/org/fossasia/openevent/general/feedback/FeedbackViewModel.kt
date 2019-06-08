package org.fossasia.openevent.general.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class FeedbackViewModel(
    private val feedbackService: FeedbackService,
    private val resource: Resource
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val mutableFeedback = MutableLiveData<List<Feedback>>()
    val feedback: LiveData<List<Feedback>> = mutableFeedback
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableProgress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = mutableProgress

    fun getAllFeedback(eventId: Long) {
        if (eventId == -1L) {
            mutableMessage.value = resource.getString(R.string.error_fetching_feedback_message)
            return
        }

        compositeDisposable += feedbackService.getFeedbackUnderEventFromDb(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableFeedback.value = it
            }, {
                mutableMessage.value = resource.getString(R.string.error_fetching_feedback_message)
                Timber.e(it, " Fail on fetching feedback for event ID: $eventId")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
