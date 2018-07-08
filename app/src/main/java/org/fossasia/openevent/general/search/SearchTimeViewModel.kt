package org.fossasia.openevent.general.search

import android.arch.lifecycle.ViewModel
import org.fossasia.openevent.general.data.Preference

class SearchTimeViewModel(private val preference: Preference) : ViewModel() {
    private val tokenKeyDate = "DATE"
    private val tokenKeyNextDate = "NEXT_DATE"

    fun saveDate(time: String) {
        preference.putString(tokenKeyDate, time)
    }

    fun saveNextDate(time: String) {
        preference.putString(tokenKeyNextDate, time)
    }
}
