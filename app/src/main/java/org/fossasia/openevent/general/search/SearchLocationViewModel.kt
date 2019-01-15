package org.fossasia.openevent.general.search

import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.LOCATION_SAVED
import org.fossasia.openevent.general.data.Preference

class SearchLocationViewModel(private val preference: Preference) : ViewModel() {
    private val tokenKey = "LOCATION"

    fun saveSearch(query: String) {
        preference.putString(tokenKey, query)
        preference.putBoolean(LOCATION_SAVED, true)
    }
}
