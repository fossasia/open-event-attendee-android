package org.fossasia.openevent.general.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.KoinComponent
import org.koin.core.inject

class TokenAuthenticator : Authenticator, KoinComponent {

    val tokenService: RefreshTokenService by inject()
    val authHolder: AuthHolder by inject()

    /**
     * Authenticator for when the authToken need to be refresh and updated
     * everytime we get a 401 error code
     */

    override fun authenticate(route: Route?, response: Response): Request? {

        val loginResponse = tokenService.refreshToken()

        return if (loginResponse.isSuccessful) {
            /**
             * Replace the existing tokens with the new tokens
             **/
            loginResponse.body()?.let {
                authHolder.accessToken = it.accessToken
                authHolder.refreshToken = it.refreshToken

                val newToken = "JWT ${it.accessToken}"

                response.request.newBuilder()
                    .addHeader("Authorization", newToken)
                    .build()
            }
        } else {
            authHolder.accessToken = null
            authHolder.refreshToken = null
            response.request
        }
    }
}
