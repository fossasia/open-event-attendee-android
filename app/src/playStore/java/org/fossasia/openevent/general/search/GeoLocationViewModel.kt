package org.fossasia.openevent.general.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class GeoLocationViewModel : ViewModel() {
    private val mutableLocation = MutableLiveData<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableVisibility = MutableLiveData<Boolean>()
    val currentLocationVisibility: LiveData<Boolean> = mutableVisibility

    @SuppressLint("MissingPermission")
    fun configure(activity: Activity?) {
        if (activity == null) return
        val service = activity.getSystemService(Context.LOCATION_SERVICE)
        var enabled = false
        if (service is LocationManager) enabled = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!enabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivity(intent)
            return
        }
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        try {
                            val geocoder = Geocoder(activity, Locale.getDefault())
                            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 2)
                            for (address: Address in addresses) {
                                if (address.adminArea != null) {
                                    mutableLocation.value = address.adminArea
                                }
                            }
                        } catch (exception: IOException) {
                            Timber.e(exception, "Error Fetching Location")
                        }
                    }
                }
            }
        }
        LocationServices
            .getFusedLocationProviderClient(activity)
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }
}
