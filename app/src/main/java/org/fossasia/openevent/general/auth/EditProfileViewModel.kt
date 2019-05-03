package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber
import java.io.File

class EditProfileViewModel(
    private val authService: AuthService,
    private val authHolder: AuthHolder,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private var updatedImageTemp = MutableLiveData<File>()
    var avatarUpdated = false
    var encodedImage: String? = null

    fun isLoggedIn() = authService.isLoggedIn()

    /**
     *  @param firstName updated firstName
     *  @param lastName updated lastName
     */
    fun updateProfile(firstName: String, lastName: String) {
        if (encodedImage.isNullOrEmpty()) {
            updateUser(null, firstName, lastName)
            return
        }
        compositeDisposable += authService.uploadImage(UploadImage(encodedImage))
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                updateUser(it.url, firstName, lastName)
                mutableMessage.value = resource.getString(R.string.image_upload_success_message)
                Timber.d("Image uploaded ${it.url}")
            }) {
                mutableMessage.value = resource.getString(R.string.image_upload_error_message)
                Timber.e(it, "Error uploading user!")
            }
    }

    private fun updateUser(url: String?, firstName: String, lastName: String) {
        val id = authHolder.getId()
        if (firstName.isEmpty() || lastName.isEmpty()) {
            mutableMessage.value = resource.getString(R.string.provide_name_message)
            return
        }
        compositeDisposable += authService.updateUser(
            User(
                id = id,
                firstName = firstName,
                lastName = lastName,
                avatarUrl = url
            ), id
        )
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableMessage.value = resource.getString(R.string.user_update_success_message)
                Timber.d("User updated")
            }) {
                mutableMessage.value = resource.getString(R.string.user_update_error_message)
                Timber.e(it, "Error updating user!")
            }
    }

    fun setUpdatedTempFile(file: File) {
        updatedImageTemp.value = file
    }

    fun getUpdatedTempFile(): MutableLiveData<File> {
        return updatedImageTemp
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
