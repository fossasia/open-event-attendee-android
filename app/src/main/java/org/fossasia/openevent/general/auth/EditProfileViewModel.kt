package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import timber.log.Timber

const val USER_UPDATED = "User updated successfully!"

class EditProfileViewModel(
    private val authService: AuthService,
    private val authHolder: AuthHolder
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
                mutableMessage.value = "Image uploaded successfully!"
                Timber.d("Image uploaded " + it.url)
            }) {
                mutableMessage.value = "Error uploading image!"
                Timber.e(it, "Error uploading user!")
            })
    }

    fun updateUser(url: String?, firstName: String, lastName: String) {
        val id = authHolder.getId()
        if (firstName.isEmpty() || lastName.isEmpty()) {
            mutableMessage.value = "Please provide first name and last name!"
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
                mutableMessage.value = USER_UPDATED
                Timber.d("User updated")
            }) {
                mutableMessage.value = "Error updating user!"
                Timber.e(it, "Error updating user!")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
