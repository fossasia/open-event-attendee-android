package org.fossasia.openevent.general.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Preference
import timber.log.Timber

const val API_URL = "apiUrl"

class SettingsViewModel(
    private val authService: AuthService,
    private val preference: Preference
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableSnackBar = SingleLiveEvent<String>()
    val snackBar: LiveData<String> = mutableSnackBar
    private val mutableUpdatedPassword = MutableLiveData<String>()
    val updatedPassword: LiveData<String> = mutableUpdatedPassword

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

    fun getMarketAppLink(packageName: String): String {
        return "market://details?id=" + packageName
    }

    fun getMarketWebLink(packageName: String): String {
        return "https://play.google.com/store/apps/details?id=" + packageName
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        compositeDisposable += authService.changePassword(oldPassword, newPassword)
            .withDefaultSchedulers()
            .subscribe({
                if (it.passwordChanged) {
                    mutableSnackBar.value = "Password changed successfully!"
                    mutableUpdatedPassword.value = newPassword
                }
            }, {
                if (it.message.toString() == "HTTP 400 BAD REQUEST")
                    mutableSnackBar.value = "Incorrect Old Password provided!"
                else mutableSnackBar.value = "Unable to change password!"
            })
    }

    fun getApiUrl(): String {
        return preference.getString(API_URL) ?: BuildConfig.DEFAULT_BASE_URL
    }

    fun changeApiUrl(url: String) {
        preference.putString(API_URL, url)
        logout()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
