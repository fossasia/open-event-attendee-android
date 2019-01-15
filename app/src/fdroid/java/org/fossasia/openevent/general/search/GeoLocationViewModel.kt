package org.fossasia.openevent.general.search

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GeoLocationViewModel : ViewModel() {
    private val mutableLocation = MutableLiveData<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableVisibility = MutableLiveData<Boolean>()
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility

    fun configure(activity: Activity?) {
        mutableVisibility.value = false
        return
    }
}
