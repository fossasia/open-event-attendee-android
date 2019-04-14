package org.fossasia.openevent.general.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.common.SingleLiveEvent
import timber.log.Timber

class SettingsViewModel(private val authService: AuthService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableSnackBar = SingleLiveEvent<String>()
    val snackBar: LiveData<String> = mutableSnackBar

    fun isLoggedIn() = authService.isLoggedIn()

    fun logout() {
        compositeDisposable.add(authService.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Logged out!")
                }) {
                    Timber.e(it, "Failure Logging out!")
                })
    }

    fun getMarketAppLink(packageName: String): String {
        return "market://details?id=" + packageName
    }

    fun getMarketWebLink(packageName: String): String {
        return "https://play.google.com/store/apps/details?id=" + packageName
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        compositeDisposable.add(authService.changePassword(oldPassword, newPassword)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.passwordChanged) mutableSnackBar.value = "Password changed successfully!"
            }, {
                if (it.message.toString() == "HTTP 400 BAD REQUEST")
                    mutableSnackBar.value = "Incorrect Old Password provided!"
                else mutableSnackBar.value = "Unable to change password!"
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
