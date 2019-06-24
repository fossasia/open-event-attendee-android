package org.fossasia.openevent.general.search.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.common.SingleLiveEvent

class GeoLocationViewModel(locationService: LocationService) : ViewModel() {
    private val mutableLocation = MutableLiveData<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableVisibility = MutableLiveData<Boolean>(false)
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility
    private val mutableOpenLocationSettings = MutableLiveData<Boolean>()
    val openLocationSettings: LiveData<Boolean> = mutableOpenLocationSettings
    private val mutableErrorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = mutableErrorMessage

    fun configure() {
        mutableVisibility.value = false
        return
    }
}
