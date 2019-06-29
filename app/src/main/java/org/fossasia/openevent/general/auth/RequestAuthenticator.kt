package org.fossasia.openevent.general.auth

import okhttp3.Interceptor
import okhttp3.Response

class RequestAuthenticator(private val authHolder: AuthHolder) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authorization = authHolder.getAuthorization()
        val original = chain.request()
        return if (authorization != null) {
            val request = original.newBuilder()
                .header("Authorization", authorization)
                .build()
            chain.proceed(request)
        } else
            chain.proceed(original)
    }
}
