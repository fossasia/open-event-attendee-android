package org.fossasia.openevent.general.event

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.OpenEventGeneral
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object EventUtils {

    @JvmStatic
    private val sharedResource by lazy {
        Resource()
    }

    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private const val frontendUrl = "https://open-event-frontend-dev.herokuapp.com/e/"

    fun getSharableInfo(event: Event, resource: Resource = sharedResource): String {
        val description = event.description.nullToEmpty()
        val identifier = event.identifier
        val eventUrl = frontendUrl + identifier

        val message = StringBuilder()

        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)

        message.append(resource.getString(R.string.event_name)).append(event.name).append("\n\n")
        if (description.stripHtml().nullToEmpty().isNotEmpty())
            message.append(resource.getString(R.string.event_description))
                .append(event.description.nullToEmpty().stripHtml()).append("\n\n")
        message.append(resource.getString(R.string.starts_on))
                .append(startsAt.format(dateFormat)).append(" ")
                .append(startsAt.format(timeFormat)).append("\n")
        message.append(resource.getString(R.string.ends_on))
                .append(endsAt.format(dateFormat)).append(" ")
                .append(endsAt.format(timeFormat)).append("\n")
        message.append(resource.getString(R.string.event_location))
                .append(event.locationName)
        if (eventUrl.isNotEmpty()) message.append("\n")
                .append(resource.getString(R.string.event_link))
                .append(eventUrl)

        return message.toString()
    }

    fun loadMapUrl(event: Event) = "geo:<${event.latitude}>,<${event.longitude}>" +
        "?q=<${event.latitude}>,<${event.longitude}>"

    fun getEventDateTime(dateString: String, timeZone: String?): ZonedDateTime {
        try {
            return when (PreferenceManager.getDefaultSharedPreferences(OpenEventGeneral.appContext)
                .getBoolean("useEventTimeZone", false) && !timeZone.isNullOrBlank()) {

                true -> ZonedDateTime.parse(dateString)
                    .toOffsetDateTime()
                    .atZoneSameInstant(ZoneId.of(timeZone))
                false -> ZonedDateTime.parse(dateString)
                    .toOffsetDateTime()
                    .atZoneSameInstant(ZoneId.systemDefault())
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

    /**
     *  share event detail along with event image
     *  if image loading is successful then imageView tag will be set to String
     *  So if imageView tag is not null then share image else only event details
     *  @param eventImage to extract the image and use context
     *
     */
    fun share(event: Event, eventImage: ImageView) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, getSharableInfo(event))
        eventImage.tag?.let {
            val bmpUri = getLocalBitmapUri(eventImage)
            if (bmpUri != null) {
                sendIntent.type = "image/*"
                sendIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            }
        }
        eventImage.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
    }

    private fun getLocalBitmapUri(imageView: ImageView): Uri? {
        val drawable = imageView.drawable
        if (drawable as? BitmapDrawable == null || drawable.bitmap == null)
            return null

        val file = File(imageView.context.cacheDir, "share_image_" + System.currentTimeMillis() + ".png")
        return FileOutputStream(file)
            .use { fos ->
                drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
                Timber.d("file: %s", file.absolutePath)
                val bmpUri =
                    FileProvider.getUriForFile(imageView.context, BuildConfig.APPLICATION_ID + ".provider", file)
                Timber.d("uri: %s", bmpUri)
                bmpUri
            }
    }

    fun getTimeInISO8601(date: Date): String {
        val tz = TimeZone.getTimeZone(TimeZone.getDefault().id)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        df.timeZone = tz
        return df.format(date)
    }
}
