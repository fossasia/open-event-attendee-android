package org.fossasia.openevent.general

import org.fossasia.openevent.general.utils.EVENT_IDENTIFIER
import org.fossasia.openevent.general.utils.RESET_PASSWORD_TOKEN
import org.fossasia.openevent.general.utils.AppLinkUtils
import org.fossasia.openevent.general.utils.VERIFICATION_TOKEN
import org.fossasia.openevent.general.utils.AppLinkData
import org.junit.Test
import org.junit.Assert.assertEquals

class AppLinkUtilsTest {

    @Test
    fun `should get event link`() {
        val uri = "https://eventyay.com/e/5f6d3feb"
        assertEquals(AppLinkData(R.id.eventDetailsFragment,
            EVENT_IDENTIFIER,
            "5f6d3feb"), AppLinkUtils.getData(uri))
    }

    @Test
    fun `should get reset password link`() {
        val uri = "https://eventyay.com/reset-password?token=822980340478781748445098077144"
        assertEquals(AppLinkData(R.id.eventsFragment,
            RESET_PASSWORD_TOKEN,
            "822980340478781748445098077144"), AppLinkUtils.getData(uri))
    }

    @Test
    fun `should get verify email link`() {
        val uri = "https://eventyay.com/verify?token=WyJsaXZlLmhhcnNoaXRAaG"
        assertEquals(AppLinkData(R.id.profileFragment,
            VERIFICATION_TOKEN,
            "WyJsaXZlLmhhcnNoaXRAaG"), AppLinkUtils.getData(uri))
    }
}
