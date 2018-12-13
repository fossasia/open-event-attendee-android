package org.fossasia.openevent.general.search

import android.app.Activity
import android.view.View
import android.widget.LinearLayout

class GeoLocationUI {

    fun configure(activity: Activity, view: LinearLayout, fromSearchFragment: Boolean , searchLocationViewModel: SearchLocationViewModel){
        //Feature not available for F-droid
        view.visibility = View.GONE
    }
}
