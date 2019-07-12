package org.fossasia.openevent.general.utils

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import org.fossasia.openevent.general.R

const val EVENT_IDENTIFIER = "eventIdentifier"
const val VERIFICATION_TOKEN = "verificationToken"
const val RESET_PASSWORD_TOKEN = "resetPasswordToken"
private const val SEGMENT_VERIFY = "verify"
private const val SEGMENT_RESET_PASSWORD = "reset-password"
private const val SEGMENT_EVENT = "e"

object AppLinkUtils {
    fun getData(uri: String): AppLinkData? {
        val values = uri.split("/", "?", "=")
        return when (values[3]) {
            SEGMENT_EVENT -> AppLinkData(R.id.eventDetailsFragment, EVENT_IDENTIFIER, values.last())
            SEGMENT_VERIFY -> AppLinkData(R.id.profileFragment, VERIFICATION_TOKEN, values.last())
            SEGMENT_RESET_PASSWORD -> AppLinkData(R.id.eventsFragment, RESET_PASSWORD_TOKEN, values.last())
            else -> null
        }
    }

    fun handleIntent(intent: Intent?, navController: NavController) {
        val uri = intent?.data ?: return
        val data = getData(uri.toString()) ?: return
        val bundle = Bundle()
        bundle.putString(data.argumentKey, data.argumentValue)
        navController.navigate(data.destinationId, bundle)
    }
}
