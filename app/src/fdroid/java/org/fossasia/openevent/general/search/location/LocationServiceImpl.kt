package org.fossasia.openevent.general.search.location

import android.content.Context
import io.reactivex.Single

class LocationServiceImpl(context: Context) : LocationService {

    override fun getAdministrativeArea(): Single<String> {
        throw IllegalStateException("Attempt to use location functionality in F-Droid flavor")
    }
}
