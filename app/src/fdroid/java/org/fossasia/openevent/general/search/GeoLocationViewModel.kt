package org.fossasia.openevent.general.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.common.SingleLiveEvent

class GeoLocationViewModel(locationService: LocationService) : ViewModel() {
    private val mutableLocation = MutableLiveData<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableVisibility = MutableLiveData<Boolean>()
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility
    private val mutableOpenLocationSettings = MutableLiveData<Boolean>()
    val openLocationSettings: LiveData<Boolean> = mutableOpenLocationSettings
    private val mutableErrorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = mutableErrorMessage

    init {
        mutableVisibility.value = false
    }

    fun configure() {
        mutableVisibility.value = false
        return
    }
}
