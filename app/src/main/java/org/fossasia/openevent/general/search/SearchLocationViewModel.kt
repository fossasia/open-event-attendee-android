package org.fossasia.openevent.general.search

import android.arch.lifecycle.ViewModel
import org.fossasia.openevent.general.data.Preference

class SearchLocationViewModel(private val preference: Preference) : ViewModel() {
    private val tokenKey = "LOCATION"

    fun saveSearch(query: String) {
        preference.putString(tokenKey, query)
    }
}