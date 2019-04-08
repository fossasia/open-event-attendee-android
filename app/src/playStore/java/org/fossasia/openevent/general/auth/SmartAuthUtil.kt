package org.fossasia.openevent.general.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.common.api.ResolvableApiException

object SmartAuthUtil {
    fun getCredentialsClient(activity: Activity): CredentialsClient {
        return Credentials.getClient(activity)
    }

    fun handleResolvableApiException(rae: ResolvableApiException, activity: Activity, value: Int) {
        rae.startResolutionForResult(activity, value)
    }

    fun getEmailAddressFromIntent(data: Intent?): String? {
        return data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)?.id
    }
}
