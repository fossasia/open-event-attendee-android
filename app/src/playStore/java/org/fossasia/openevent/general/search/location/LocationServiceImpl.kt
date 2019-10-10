package org.fossasia.openevent.general.search.location

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.Single
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.location.LocationPermissionException
import org.fossasia.openevent.general.location.NoLocationSourceException
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class LocationServiceImpl(
    private val context: Context,
    private val resource: Resource
) : LocationService {

    override fun getAdministrativeArea(): Single<String> {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)

        if (locationManager !is LocationManager) {
            return Single.error(IllegalStateException())
        }

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return Single.error(NoLocationSourceException())
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        return Single.create { emitter ->
            try {
                LocationServices
                    .getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, object : LocationCallback() {

                        override fun onLocationResult(locationResult: LocationResult?) {
                            if (locationResult == null) {
                                emitter.onError(IllegalStateException())
                                return
                            }
                            try {
                                val adminArea = locationResult.getAdminArea()
                                emitter.onSuccess(adminArea)
                            } catch (e: Exception) {
                                Timber.e(e, "Error finding user current location")
                                emitter.onSuccess(resource.getString(R.string.no_location).nullToEmpty())
                            }
                        }
                    }, null)
            } catch (e: SecurityException) {
                emitter.onError(LocationPermissionException())
            }
        }
    }

    private fun LocationResult.getAdminArea(): String {
        locations.filterNotNull().forEach { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            try {
                val maxResults = 2
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, maxResults)
                val address = addresses.first { address -> address.adminArea != null }
                return address.adminArea
            } catch (exception: IOException) {
                Timber.e(exception, "Error Fetching Location")
                throw IllegalArgumentException()
            }
        }
        throw IllegalArgumentException()
    }
}
