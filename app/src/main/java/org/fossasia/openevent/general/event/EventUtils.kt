package org.fossasia.openevent.general.event

import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.nullToEmpty
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object EventUtils {

    @JvmStatic
    private val sharedResource by lazy {
        Resource()
    }

    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun getSharableInfo(event: Event, resource: Resource = sharedResource): String {
        val description = event.description.nullToEmpty()
        val eventUrl = event.externalEventUrl.nullToEmpty()

        val message = StringBuilder()

        val startsAt = getLocalizedDateTime(event.startsAt)
        val endsAt = getLocalizedDateTime(event.endsAt)

        message.append(resource.getString(R.string.event_name)).append(event.name).append("\n\n")
        if (!description.isEmpty()) message.append(resource.getString(R.string.event_description))
                .append(event.description.nullToEmpty()).append("\n\n")
        message.append(resource.getString(R.string.starts_on))
                .append(startsAt.format(dateFormat)).append(" ")
                .append(startsAt.format(timeFormat)).append("\n")
        message.append(resource.getString(R.string.ends_on))
                .append(endsAt.format(dateFormat)).append(" ")
                .append(endsAt.format(timeFormat))
        if (!eventUrl.isEmpty()) message.append("\n")
                .append(resource.getString(R.string.event_link))
                .append(eventUrl)

        return message.toString()
    }

    fun getLocalizedDateTime(dateString: String): ZonedDateTime = ZonedDateTime.parse(dateString)
            .toOffsetDateTime()
            .atZoneSameInstant(ZoneId.systemDefault())

    fun getTimeInMilliSeconds(date: String): Long{
        var testDate =  Date()
        val dateString = date.replace("T"," ")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            try {
                testDate = dateFormat.parse(dateString)
            } catch (e: ParseException) {
                Timber.e(e,"Error Parsing Date")
            }
        return testDate.time
    }
}