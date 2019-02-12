package org.fossasia.openevent.general.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.search.LocationService

class WelcomeViewModel(locationService: LocationService, preference: Preference) : ViewModel() {

    private val mutableRedirectToMain = MutableLiveData<Boolean>()
    val redirectToMain: LiveData<Boolean> = mutableRedirectToMain
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
        // Since there are no location services for f-droid, this doesn't do anything.
    }
}
