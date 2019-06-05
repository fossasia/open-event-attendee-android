package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.R
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber

class ProfileViewModel(private val authService: AuthService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage

    fun isLoggedIn() = authService.isLoggedIn()

    fun logout() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Logged out!")
            }) {
                Timber.e(it, "Failure Logging out!")
            }
    }

    fun fetchProfile() {
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                Timber.d("Response Success")
                this.mutableUser.value = user
            }) {
                Timber.e(it, "Failure")
                mutableMessage.value = resource.getString(R.string.failure)
            }
    }

    fun resendVerificationEmail(email: String) {
        compositeDisposable += authService.resendVerificationEmail(email)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
            }) {
                Timber.e(it, "Failure")
                mutableMessage.value = resource.getString(R.string.failure)
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
