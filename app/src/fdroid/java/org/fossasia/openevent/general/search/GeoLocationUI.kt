package org.fossasia.openevent.general.search

import android.app.Activity
import android.view.View
import kotlinx.android.synthetic.main.activity_search_location.*

class GeoLocationUI {

    fun configure(activity: Activity, searchLocationViewModel: SearchLocationViewModel){
        activity.currentLocation.visibility = View.GONE
    }
    fun search(activity: Activity,fromSearchFragment: Boolean , searchLocationViewModel: SearchLocationViewModel, query:String){
    }
}
