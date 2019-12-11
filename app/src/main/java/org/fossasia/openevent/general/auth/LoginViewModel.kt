package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import retrofit2.HttpException
import timber.log.Timber

class LoginViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource,
    private val eventService: EventService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableRequestTokenSuccess = MutableLiveData<LinkResetResponse>()
    val requestTokenSuccess: LiveData<LinkResetResponse> = mutableRequestTokenSuccess
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn
    private val mutableValidPassword = MutableLiveData<Boolean>()
    val validPassword: LiveData<Boolean> = mutableValidPassword

    fun isLoggedIn() = authService.isLoggedIn()

    data class LinkResetResponse(val status: Boolean, val message: String?)

    fun login(email: String, password: String) {
        if (!isConnected()) return

        val loginObservable: Single<LoginResponse> = authService.login(email, password).flatMap { loginResponse ->
            eventService.loadFavoriteEvent().flatMap { favsList ->
                val favIds = favsList.filter { favEvent -> favEvent.event != null }
                eventService.saveFavoritesEventFromApi(favIds).flatMap {
                    Single.just(loginResponse)
                }
            }
        }

        compositeDisposable += loginObservable
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
            }, {
                mutableError.value = resource.getString(R.string.login_fail_message)
            })
    }

    fun checkValidPassword(email: String, password: String) {
        compositeDisposable += authService.checkPasswordValid(email, password)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableValidPassword.value = true
            }, {
                mutableValidPassword.value = false
            })
    }

    fun sendResetPasswordEmail(email: String) {
        if (!isConnected()) return
        compositeDisposable += authService.sendResetPasswordEmail(email)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableRequestTokenSuccess.value = LinkResetResponse(true, it.message)
            }, {
                mutableRequestTokenSuccess.value = LinkResetResponse(false, getErrorMessage(it))
                mutableError.value = getErrorMessage(it)
            })
    }

    private fun getErrorMessage(error: Throwable): String? {
        return if (error is HttpException) {
            when (error.code()) {
                429 -> resource.getString(R.string.reset_mail_limit_message)
                else -> error.message()
            }
        } else {
            resource.getString(R.string.something_went_wrong_message)
        }
    }

    fun fetchProfile() {
        if (!isConnected()) return
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                Timber.d("User Fetched")
                mutableUser.value = it
            }) {
                Timber.e(it, "Failure")
                mutableError.value = resource.getString(R.string.failure)
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}
