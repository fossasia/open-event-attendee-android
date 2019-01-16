package org.fossasia.openevent.general.search

import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.data.Preference

const val SAVED_LOCATION = "LOCATION"

class SearchLocationViewModel(private val preference: Preference) : ViewModel() {

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)
    }
}
