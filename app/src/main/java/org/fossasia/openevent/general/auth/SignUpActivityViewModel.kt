package org.fossasia.openevent.general.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import timber.log.Timber

class SignUpActivityViewModel(private val authService: AuthService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val error = SingleLiveEvent<String>()
    val signedUp = MutableLiveData<User>()
    val loggedIn = SingleLiveEvent<Boolean>()

    fun signUp(email: String, password: String) {
        compositeDisposable.add(authService.signUp(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    signedUp.value = it
                    Timber.d("Success!")
                }, {
                    error.value = "Unable to SignIn"
                    Timber.d("Failed" + it)
        }))
    }

    fun loginAfterSignUp(email: String, password: String) {
        compositeDisposable.add(authService.login(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    loggedIn.value = true
                    Timber.d("Success!")
                }, {
                    error.value = "Unable to Login Automatically"
                    Timber.d("Failed" + it)
        }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}