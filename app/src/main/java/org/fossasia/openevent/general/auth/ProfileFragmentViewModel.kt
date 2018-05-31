package org.fossasia.openevent.general.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ProfileFragmentViewModel(private val authService: AuthService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val avatarUrl = MutableLiveData<String>()
    val visibility = MutableLiveData<Int>()
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()

    fun isLoggedIn() = authService.isLoggedIn()

    fun logout() = authService.logout()

    fun fetchProfile() {
        compositeDisposable.add(authService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    progress.value = false
                    visibility.value = View.GONE
                }
                .subscribe({ user ->
                    Timber.d("Response Success")
                    name.value = "${user.firstName} ${user.lastName}"
                    email.value = user.email
                    avatarUrl.value = user.avatarUrl

                }) { throwable -> Timber.e(throwable, "Failure") })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}
