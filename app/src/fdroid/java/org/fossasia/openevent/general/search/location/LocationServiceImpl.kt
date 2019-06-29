package org.fossasia.openevent.general.search.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import io.reactivex.Single
import java.lang.IllegalArgumentException
import java.util.Locale

class LocationServiceImpl(private val context: Context) : LocationService {

    @SuppressLint("MissingPermission")
    override fun getAdministrativeArea(): Single<String> {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return Single.error(IllegalStateException())
        val geoCoder = Geocoder(context, Locale.getDefault())

        return Single.create { emitter ->
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,
                    object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location == null) {
                            emitter.onError(java.lang.IllegalStateException())
                            return
                        }
                        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                        try {
                            val adminArea = addresses[0].adminArea
                            emitter.onSuccess(adminArea)
                        } catch (e: IllegalArgumentException) {
                            emitter.onError(e)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        // To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(provider: String?) {
                        // To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(provider: String?) {
                        // To change body of created functions use File | Settings | File Templates.
                    }
                })
            } catch (e: SecurityException) {
                emitter.onError(e)
            }
        }
    }
}
