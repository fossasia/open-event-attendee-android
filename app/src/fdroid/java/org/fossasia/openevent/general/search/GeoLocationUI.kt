package org.fossasia.openevent.general.search

import android.app.Activity
import android.view.View
import kotlinx.android.synthetic.main.fragment_search_location.view.currentLocation

class GeoLocationUI {

    fun configure(activity: Activity, view: View, searchLocationViewModel: SearchLocationViewModel) {
        view.currentLocation.visibility = View.GONE
    }

    fun search(
        activity: Activity,
        fromSearchFragment: Boolean,
        searchLocationViewModel: SearchLocationViewModel,
        query: String
    ) {
    }
}
