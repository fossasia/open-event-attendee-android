package org.fossasia.openevent.general.speakercall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.UploadImage
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber
import java.io.File

class EditSpeakerViewModel(
    private val resource: Resource,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val speakerService: SpeakerService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableProgress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSpeaker = MutableLiveData<Speaker>()
    val speaker: LiveData<Speaker> = mutableSpeaker
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableSubmitSuccess = MutableLiveData<Boolean>()
    val submitSuccess: LiveData<Boolean> = mutableSubmitSuccess
    private var updatedImageTemp = MutableLiveData<File>()

    var encodedImage: String? = null

    fun getId(): Long = authHolder.getId()

    fun loadUser(userId: Long) {
        compositeDisposable += authService.getProfile(userId)
            .withDefaultSchedulers()
            .doOnSubscribe {
            mutableProgress.value = true
        }.subscribe({
            mutableProgress.value = false
            mutableUser.value = it
        }, {
            mutableProgress.value = false
            Timber.e(it, "Fail on fetching speaker id $userId")
        })
    }

    fun loadSpeaker(speakerId: Long) {
        if (speakerId == -1L) {
            mutableMessage.value = resource.getString(R.string.no_speaker_info)
            return
        }

        compositeDisposable += speakerService.fetchSpeaker(speakerId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableSpeaker.value = it
            }, {
                mutableProgress.value = false
                Timber.e(it, "Fail on fetching speaker id $speakerId")
                mutableMessage.value = resource.getString(R.string.no_speaker_info)
            })
    }

    fun setUpdatedTempFile(file: File) {
        updatedImageTemp.value = file
    }

    fun getUpdatedTempFile(): MutableLiveData<File> {
        return updatedImageTemp
    }

    fun submitSpeaker(speaker: Speaker) {
        compositeDisposable += speakerService.addSpeaker(speaker)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableSubmitSuccess.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.create_speaker_fail_message)
                mutableSubmitSuccess.value = false
            })
    }

    fun editSpeaker(speaker: Speaker) {
        val image = encodedImage
        if (!image.isNullOrEmpty()) {
            uploadImageAndEditSpeaker(speaker, image)
            return
        }
        compositeDisposable += speakerService.editSpeaker(speaker)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableSubmitSuccess.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.update_speaker_fail_message)
                mutableSubmitSuccess.value = false
            })
    }

    private fun uploadImageAndEditSpeaker(speaker: Speaker, avatar: String) {

        compositeDisposable += authService.uploadImage(UploadImage(avatar))
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableMessage.value = resource.getString(R.string.image_upload_success_message)
                encodedImage = null
                val newSpeaker = speaker.copy(photoUrl = it.url)
                editSpeaker(newSpeaker)
                Timber.d("Image uploaded ${it.url}")
            }) {
                mutableMessage.value = resource.getString(R.string.image_upload_error_message)
                Timber.e(it, "Error uploading user!")
            }
    }
}
