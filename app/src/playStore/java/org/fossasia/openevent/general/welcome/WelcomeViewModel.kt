package org.fossasia.openevent.general.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.location.LocationPermissionException
import org.fossasia.openevent.general.location.NoLocationSourceException
import org.fossasia.openevent.general.search.LocationService

const val SAVED_LOCATION = "LOCATION"

class WelcomeViewModel(private val locationService: LocationService, private val preference: Preference) : ViewModel() {

    private val mutableRedirectToMain = MutableLiveData<Boolean>()
    val redirectToMain: LiveData<Boolean> = mutableRedirectToMain
    private val mutableVisibility = MutableLiveData<Boolean>()
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility
    private val mutableOpenLocationSettings = MutableLiveData<Boolean>()
    val openLocationSettings: LiveData<Boolean> = mutableOpenLocationSettings
    private val mutableErrorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = mutableErrorMessage

    private val compositeDisposable = CompositeDisposable()

    fun configure() {
        compositeDisposable.add(locationService.getAdministrativeArea()
            .subscribe(
                { adminArea ->
                    preference.putString(SAVED_LOCATION, adminArea)
                    mutableRedirectToMain.value = true
                },
                { error ->
                    when (error) {
                        is NoLocationSourceException -> {
                            mutableErrorMessage.value = "No location sources are enabled"
                            mutableOpenLocationSettings.value = true
                        }
                        is LocationPermissionException -> {
                            mutableErrorMessage.value = "Please give the location permission"
                        }
                        else -> {
                            mutableErrorMessage.value = "Something went wrong"
                            mutableVisibility.value = false
                        }
                    }
                }
            ))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
