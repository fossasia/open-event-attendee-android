package org.fossasia.openevent.general.search.location

import io.reactivex.Single

/**
 * Implementations of this interface provide functionality related to the user location.
 * */
interface LocationService {

    /**
     * Gives the administrative area of the current location the user is in.
     * */
    fun getAdministrativeArea(): Single<String>
}
