package org.fossasia.openevent.general.event

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Resource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import java.time.ZoneId
import java.util.TimeZone

class EventUtilsTest {

    @MockK
    lateinit var resource: Resource

    private var timeZone: TimeZone? = null

    @Before
    fun setUp() {
        // Set fixed local time zone for tests
        timeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")))
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(timeZone)
    }

    private fun getEvent(
        id: Long = 34,
        name: String = "Eva Event",
        identifier: String = "abcdefgh",
        startsAt: String = "2008-09-15T15:53:00+05:00",
        endsAt: String = "2008-09-19T19:25:00+05:00",
        timeZone: String = "Asia/Kolkata",
        description: String? = null,
        link: String? = null
    ) =
            Event(id, name, identifier, startsAt, endsAt, timeZone,
                    description = description, externalEventUrl = link)

    private fun getEventDateTime(dateTime: String, timeZone: String): ZonedDateTime
        = EventUtils.getEventDateTime(dateTime, timeZone)

    private fun setupStringMock() {
        every { resource.getString(R.string.event_name) }.returns("Event Name : ")
        every { resource.getString(R.string.event_description) }.returns("Event Description : ")
        every { resource.getString(R.string.starts_on) }.returns("Starts On : ")
        every { resource.getString(R.string.start_time) }.returns("Start Time : ")
        every { resource.getString(R.string.ends_on) }.returns("Ends On : ")
        every { resource.getString(R.string.end_time) }.returns("End Time : ")
        every { resource.getString(R.string.event_link) }.returns("Event Link : ")
    }

    @Test
    fun `should get sharable information`() {
        val event = getEvent()
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : https://open-event-frontend-dev.herokuapp.com/e/abcdefgh
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get sharable information with description`() {
        val event = getEvent(description = "Amazing Event")
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Event Description : Amazing Event

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : https://open-event-frontend-dev.herokuapp.com/e/abcdefgh
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get sharable information with link`() {
        val event = getEvent(identifier = "abcdefgh")
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : https://open-event-frontend-dev.herokuapp.com/e/abcdefgh
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get sharable information complete`() {
        val event = getEvent(description = "Fresher", identifier = "abcdefgh")
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Event Description : Fresher

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : https://open-event-frontend-dev.herokuapp.com/e/abcdefgh
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get timezone name`() {
        val event = getEvent()
        val localizedDateTime = getEventDateTime(event.startsAt, event.timezone)
        assertEquals("""
           IST
            """.trimIndent(), EventUtils.getFormattedTimeZone(localizedDateTime))
    }

    @Test
    fun `should get formatted time`() {
        val event = getEvent()
        val localizedDateTime = getEventDateTime(event.startsAt, event.timezone)
        assertEquals("""
           04:23 PM
            """.trimIndent(), EventUtils.getFormattedTime(localizedDateTime))
    }

    @Test
    fun `should get formatted date and time without year`() {
        val event = getEvent()
        val localizedDateTime = getEventDateTime(event.startsAt, event.timezone)
        assertEquals("""
          Monday, Sep 15
            """.trimIndent(), EventUtils.getFormattedDateWithoutYear(localizedDateTime))
    }

    @Test
    fun `should get formatted date short`() {
        val event = getEvent()
        val localizedDateTime = getEventDateTime(event.startsAt, event.timezone)
        assertEquals("""
          Mon, Sep 15
            """.trimIndent(), EventUtils.getFormattedDateShort(localizedDateTime))
    }

    @Test
    fun `should get formatted date`() {
        val event = getEvent()
        val localizedDateTime = getEventDateTime(event.startsAt, event.timezone)
        assertEquals("""
          Monday, Sep 15, 2008
            """.trimIndent(), EventUtils.getFormattedDate(localizedDateTime))
    }

    @Test
    fun `should get formatted date range when start and end date are not same`() {
        val event = getEvent()
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
         Mon, Sep 15, 04:23 PM
            """.trimIndent(), EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range when start and end date are same`() {
        val event = getEvent(endsAt = "2008-09-15T15:53:00+05:00")
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          Monday, Sep 15
            """.trimIndent(), EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range when start and end date are not same in event details`() {
        val event = getEvent()
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          - Fri, Sep 19, 07:55 PM IST
            """.trimIndent(), EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range when start and end date are same in event details`() {
        val event = getEvent(endsAt = "2008-09-15T15:53:00+05:00")
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          04:23 PM - 04:23 PM IST
            """.trimIndent(), EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range when start and end date are not same in details`() {
        val event = getEvent()
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          Monday, Sep 15, 2008 at 04:23 PM - Friday, Sep 19, 2008 at 07:55 PM (IST)
            """.trimIndent(), EventUtils.getFormattedDateTimeRangeDetailed(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range when start and end date are same in details`() {
        val event = getEvent(endsAt = "2008-09-15T15:53:00+05:00")
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          Monday, Sep 15, 2008 from 04:23 PM to 04:23 PM (IST)
            """.trimIndent(), EventUtils.getFormattedDateTimeRangeDetailed(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range bulleted when start and end date are not same in details`() {
        val event = getEvent()
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          Mon, Sep 15 - Fri, Sep 19 • 04:23 PM IST
            """.trimIndent(), EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt))
    }

    @Test
    fun `should get formatted date range bulleted when start and end date are same in details`() {
        val event = getEvent(endsAt = "2008-09-15T15:53:00+05:00")
        val startsAt = getEventDateTime(event.startsAt, event.timezone)
        val endsAt = getEventDateTime(event.endsAt, event.timezone)
        assertEquals("""
          Mon, Sep 15 • 04:23 PM IST
            """.trimIndent(), EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt))
    }
}
