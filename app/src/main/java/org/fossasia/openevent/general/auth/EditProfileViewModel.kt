package org.fossasia.openevent.general.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

const val USER_UPDATED = "User updated successfully!"

class EditProfileViewModel(private val authService: AuthService, private val authHolder: AuthHolder) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val user = MutableLiveData<User>()
    val message = MutableLiveData<String>()

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
                    progress.value = true
                }
                .doFinally {
                    progress.value = false
                }
                .subscribe({
                    updateUser(it.url, firstName, lastName)
                    message.value = "Image uploaded successfully!"
                    Timber.d("Image uploaded " + it.url)
                }) {
                    message.value = "Error uploading image!"
                    Timber.e(it, "Error uploading user!")
                })
    }

    fun updateUser(url: String?, firstName: String, lastName: String) {
        val id = authHolder.getId()
        if (firstName.isEmpty() || lastName.isEmpty()) {
            message.value = "Please provide first name and last name!"
            return
        }
        compositeDisposable.add(authService.updateUser(User(id = id, firstName = firstName, lastName = lastName, avatarUrl = url), id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }
                .doFinally {
                    progress.value = false
                }
                .subscribe({
                    message.value = USER_UPDATED
                    Timber.d("User updated")
                }) {
                    message.value = "Error updating user!"
                    Timber.e(it, "Error updating user!")
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
