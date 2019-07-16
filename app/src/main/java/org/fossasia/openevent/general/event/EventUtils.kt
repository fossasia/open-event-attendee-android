package org.fossasia.openevent.general.event

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import org.fossasia.openevent.general.OpenEventGeneral
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.settings.LOCAL_TIMEZONE
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object EventUtils {

    fun loadMapUrl(event: Event) = "geo:<${event.latitude}>,<${event.longitude}>" +
        "?q=<${event.latitude}>,<${event.longitude}>"

    fun getEventDateTime(dateString: String, timeZone: String?): ZonedDateTime {
        try {
            return when (PreferenceManager.getDefaultSharedPreferences(OpenEventGeneral.appContext)
                .getBoolean(LOCAL_TIMEZONE, false) && !timeZone.isNullOrBlank()) {

                true -> ZonedDateTime.parse(dateString)
                    .toOffsetDateTime()
                    .atZoneSameInstant(ZoneId.systemDefault())
                false -> ZonedDateTime.parse(dateString)
                    .toOffsetDateTime()
                    .atZoneSameInstant(ZoneId.of(timeZone))
            }
        } catch (e: NullPointerException) {
            return ZonedDateTime.parse(dateString)
                .toOffsetDateTime()
                .atZoneSameInstant(ZoneId.systemDefault())
        }
    }

    fun getTimeInMilliSeconds(dateString: String, timeZone: String?): Long {
        return getEventDateTime(dateString, timeZone).toInstant().toEpochMilli()
    }

    fun getFormattedDate(date: ZonedDateTime): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, y")
        try {
            return dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            return ""
        }
    }

    fun getSimpleFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            return dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            return ""
        }
    }

    fun getFormattedDateShort(date: ZonedDateTime): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
        try {
            return dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            return ""
        }
    }

    fun getFormattedDateWithoutYear(date: ZonedDateTime): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        try {
            return dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            return ""
        }
    }

    fun getFormattedTime(date: ZonedDateTime): String {
        val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        try {
            return timeFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            return ""
        }
    }

    fun getFormattedTimeZone(date: ZonedDateTime): String {
        val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("z")
        try {
            return timeFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            return ""
        }
    }

    fun getFormattedEventDateTimeRange(startsAt: ZonedDateTime, endsAt: ZonedDateTime): String {
        val startingDate = getFormattedDate(startsAt)
        val endingDate = getFormattedDate(endsAt)
        try {
            if (startingDate != endingDate)
                return "${getFormattedDateShort(startsAt)}, ${getFormattedTime(startsAt)}"
            else
                return "${getFormattedDateWithoutYear(startsAt)}"
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            return ""
        }
    }

    fun getFormattedEventDateTimeRangeSecond(startsAt: ZonedDateTime, endsAt: ZonedDateTime): String {
        val startingDate = getFormattedDate(startsAt)
        val endingDate = getFormattedDate(endsAt)
        try {
            if (startingDate != endingDate)
                return "- ${getFormattedDateShort(endsAt)}, ${getFormattedTime(endsAt)} ${getFormattedTimeZone(endsAt)}"
            else
                return "${getFormattedTime(startsAt)} - ${getFormattedTime(endsAt)} ${getFormattedTimeZone(endsAt)}"
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            return ""
        }
    }

    fun getFormattedDateTimeRangeDetailed(startsAt: ZonedDateTime, endsAt: ZonedDateTime): String {
        val startingDate = getFormattedDate(startsAt)
        val endingDate = getFormattedDate(endsAt)
        return try {
            if (startingDate != endingDate)
                "$startingDate at ${getFormattedTime(startsAt)} - $endingDate" +
                    " at ${getFormattedTime(endsAt)} (${getFormattedTimeZone(endsAt)})"
            else
                "$startingDate from ${getFormattedTime(startsAt)}" +
                    " to ${getFormattedTime(endsAt)} (${getFormattedTimeZone(endsAt)})"
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            ""
        }
    }

    fun getFormattedDateTimeRangeBulleted(startsAt: ZonedDateTime, endsAt: ZonedDateTime): String {
        val startingDate = getFormattedDateShort(startsAt)
        val endingDate = getFormattedDateShort(endsAt)
        try {
            if (startingDate != endingDate)
                return "$startingDate - $endingDate • ${getFormattedTime(startsAt)} ${getFormattedTimeZone(startsAt)}"
            else
                return "$startingDate • ${getFormattedTime(startsAt)} ${getFormattedTimeZone(startsAt)}"
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting time")
            return ""
        }
    }

    fun getFormattedDateWithoutWeekday(date: ZonedDateTime): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, y")
        return try {
            dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            ""
        }
    }

    fun getFormattedWeekDay(date: ZonedDateTime): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE")
        return try {
            dateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error formatting Date")
            ""
        }
    }

    fun getDayDifferenceFromToday(date: String): Long {
        return (System.currentTimeMillis() - EventUtils.getTimeInMilliSeconds(date, null)) / (1000 * 60 * 60 * 24)
    }

    /**
     *  share event detail along with event image
     *  if image loading is successful then imageView tag will be set to String
     *  So if imageView tag is not null then share image else only event details
     *
     */
    fun share(event: Event, context: Context) {
        val sendIntent = Intent()
        val resources = Resource()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, event.name)
        sendIntent.putExtra(Intent.EXTRA_TEXT,
            "https://${resources.getString(R.string.FRONTEND_HOST)}/e/${event.identifier}")
        context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
    }

    fun getTimeInISO8601(date: Date): String {
        val tz = TimeZone.getTimeZone(TimeZone.getDefault().id)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        df.timeZone = tz
        return df.format(date)
    }
}
