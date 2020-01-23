package org.fossasia.openevent.general

import org.fossasia.openevent.general.auth.EditProfileFragment
import org.junit.Assert.assertEquals
import org.junit.Test

class RegExTest {
    val frag = EditProfileFragment()
    @Test
    fun `protocol removal when loaded`() {
        assertEquals("abcdefg", frag.loadStringRegEx("https://instagram.com/abcdefg"))
    }

    @Test
    fun `anomaly removal when updating`() {
        assertEquals("helloworld", frag.updateStringRegEx("h@ello@wo ,   rld"))
    }

    @Test
    fun `tests if the username is correct`() {
        assertEquals(true, frag.hasErrors("@username"))
    }
}
