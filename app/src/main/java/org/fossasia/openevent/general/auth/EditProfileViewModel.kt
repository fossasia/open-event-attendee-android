package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber

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

    fun isLoggedIn() = authService.isLoggedIn()

    fun updateProfile(encodedImage: String?, firstName: String, lastName: String) {
        if (encodedImage.isNullOrEmpty()) {
            updateUser(null, firstName, lastName)
            return
        }
        compositeDisposable.add(authService.uploadImage(UploadImage(encodedImage))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
            })
    }

    fun updateUser(url: String?, firstName: String, lastName: String) {
        val id = authHolder.getId()
        if (firstName.isEmpty() || lastName.isEmpty()) {
            mutableMessage.value = resource.getString(R.string.provide_name_message)
            return
        }
        compositeDisposable.add(authService.updateUser(
            User(
                id = id,
                firstName = firstName,
                lastName = lastName,
                avatarUrl = url
            ), id
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
