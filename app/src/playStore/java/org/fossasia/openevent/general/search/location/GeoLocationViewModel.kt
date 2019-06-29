package org.fossasia.openevent.general.search.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.location.LocationPermissionException
import org.fossasia.openevent.general.location.NoLocationSourceException

class GeoLocationViewModel(private val locationService: LocationService) : ViewModel() {
    private val mutableLocation = MutableLiveData<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableVisibility = MutableLiveData<Boolean>()
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility
    private val mutableOpenLocationSettings = MutableLiveData<Boolean>()
    val openLocationSettings: LiveData<Boolean> = mutableOpenLocationSettings
    private val mutableErrorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = mutableErrorMessage

    private val compositeDisposable = CompositeDisposable()

    fun configure() {
        compositeDisposable += locationService.getAdministrativeArea()
            .subscribe(
                { adminArea ->
                    mutableLocation.value = adminArea
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
            )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
