package org.fossasia.openevent.general.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class RequestAuthenticator(private val authHolder: AuthHolder) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val authorization: String? = authHolder.getAuthorization()
        return if (response.request().header("Authorization") == null && authorization != null) {
            response.request().newBuilder()
                    .header("Authorization", authorization)
                    .build()
        } else null // Give up, we already tried authenticating
    }

}