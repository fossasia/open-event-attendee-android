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
import java.time.ZoneId
import java.util.*

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

    private fun getEvent(id: Long = 34,
                         name: String = "Eva Event",
                         identifier: String = "abcdefgh",
                         startsAt: String = "2008-09-15T15:53:00+05:00",
                         endsAt: String = "2008-09-19T19:25:00+05:00",
                         timeZone: String = "Asia/Kolkata",
                         description: String? = null,
                         link: String? = null) =
            Event(id, name, identifier, startsAt, endsAt, timeZone,
                    description = description, externalEventUrl = link)

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
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get sharable information with link`() {
        val event = getEvent(link = "http://goo.gl")
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : http://goo.gl
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

    @Test
    fun `should get sharable information complete`() {
        val event = getEvent(description = "Fresher", link = "http://fresh.er")
        setupStringMock()
        assertEquals("""
            Event Name : Eva Event

            Event Description : Fresher

            Starts On : 15 Sep 2008 04:23 PM
            Ends On : 19 Sep 2008 07:55 PM
            Event Link : http://fresh.er
            """.trimIndent(), EventUtils.getSharableInfo(event, resource))
    }

}