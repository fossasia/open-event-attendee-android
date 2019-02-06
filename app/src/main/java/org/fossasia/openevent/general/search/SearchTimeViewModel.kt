package org.fossasia.openevent.general.search

import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.data.Preference

class SearchTimeViewModel(private val preference: Preference) : ViewModel() {
    companion object {
        val tokenKeyNextDate = "NEXT_DATE"
        val tokenKeyNextToNextDate = "NEXT_TO_NEXT_DATE"
        val tokenKeyWeekendDate = "WEEKEND"
        val tokenKeyWeekendNextDate = "WEEKEND_NEXT_DATE"
        val tokenKeyNextMonth = "NEXT_MONTH"
        val tokenKeyNextToNextMonth = "NEXT_TO_NEXT_MONTH"
    }

    fun saveNextDate(time: String) {
        preference.putString(tokenKeyNextDate, time)
    }

    fun saveNextToNextDate(time: String) {
        preference.putString(tokenKeyNextToNextDate, time)
    }

    fun saveWeekendDate(time: String) {
        preference.putString(tokenKeyWeekendDate, time)
    }

    fun saveNextToWeekendDate(time: String) {
        preference.putString(tokenKeyWeekendNextDate, time)
    }

    fun saveNextMonth(time: String) {
        preference.putString(tokenKeyNextMonth, time)
    }

    fun saveNextToNextMonth(time: String) {
        preference.putString(tokenKeyNextToNextMonth, time)
    }
}
