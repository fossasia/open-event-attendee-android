package org.fossasia.openevent.general.utils

import org.fossasia.openevent.general.event.EventUtils
import java.util.Calendar

object DateTimeUtils {
    fun getNextDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }

    fun getNextToNextDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 2)
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }

    fun getWeekendDate(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        if (today != Calendar.SATURDAY) {
            val offset = Calendar.SATURDAY - today
            calendar.add(Calendar.DATE, offset)
        }
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }

    fun getNextToWeekendDate(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        if (today != Calendar.SATURDAY) {
            val offset = Calendar.SATURDAY - today
            calendar.add(Calendar.DATE, offset)
        }
        calendar.add(Calendar.DATE, 1)
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }

    fun getNextMonth(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val offset = 30 - today
        calendar.add(Calendar.DATE, offset)
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }

    fun getNextToNextMonth(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val offset = 30 - today
        calendar.add(Calendar.DATE, offset)
        calendar.add(Calendar.MONTH, 1)
        return EventUtils.getSimpleFormattedDate(calendar.time)
    }
}
