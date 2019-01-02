package org.fossasia.openevent.general.search

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_search_location.currentLocation
import org.fossasia.openevent.general.MainActivity
import timber.log.Timber
import java.io.IOException
import java.util.Locale

private const val TO_SEARCH: String = "ToSearchFragment"

class GeoLocationUI {

    @SuppressLint("MissingPermission")
    fun configure(activity: Activity, searchLocationViewModel: SearchLocationViewModel) {
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            return
        }
        val service = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivity(intent)
        }
        activity.currentLocation.setOnClickListener {
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
                                    if (address.locality != null) {
                                        search(activity, SearchLocationActivity().fromSearchFragment,
                                            searchLocationViewModel, address.locality)
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

    fun search(
        activity: Activity,
        fromSearchFragment: Boolean,
        searchLocationViewModel: SearchLocationViewModel,
        query: String
    ) {
        searchLocationViewModel.saveSearch(query)
        val startMainActivity = Intent(activity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (fromSearchFragment) {
            val searchBundle = Bundle()
            searchBundle.putBoolean(TO_SEARCH, true)
            startMainActivity.putExtras(searchBundle)
        }
        activity.startActivity(startMainActivity)
    }
}
