package org.fossasia.openevent.general.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ProfileFragmentViewModel(private val authService: AuthService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val user = MutableLiveData<User>()
    val error = MutableLiveData<String>()

    fun isLoggedIn() = authService.isLoggedIn()

    fun logout() = authService.logout()

    fun fetchProfile() {
        compositeDisposable.add(authService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally {
                    progress.value = false
                }.subscribe({ user ->
                    Timber.d("Response Success")
                    this.user.value = user
                }) {
                    Timber.e(it, "Failure")
                    error.value = "Failure"
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}