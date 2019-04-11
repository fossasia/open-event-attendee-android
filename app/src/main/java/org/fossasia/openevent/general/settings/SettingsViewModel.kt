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

    fun deleteAccount() {
        compositeDisposable.add(authService.sendDeleteAccountRequest()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e(it.meta.message)
                if (it.meta.message == "Object successfully deleted") {
                    mutableSnackBar.value = "Account deleted successfully!"
                    logout()
                }
            }) {
                Timber.e(it, "Failure deleting account!")
                mutableSnackBar.value = "Failed to delete account."
            })
    }

    fun getMarketAppLink(packageName: String): String {
        return "market://details?id=" + packageName
    }

    fun getMarketWebLink(packageName: String): String {
        return "https://play.google.com/store/apps/details?id=" + packageName
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
