package org.fossasia.openevent.general.auth

import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.utils.JWTUtils

private const val TOKEN_KEY = "TOKEN"

class AuthHolder(private val preference: Preference) {

    var token: String? = null
        get() {
            return preference.getString(TOKEN_KEY)
        }
        set(value) {
            if (value != null && JWTUtils.isExpired(value))
                throw IllegalStateException("Cannot set expired token")
            field = value
            preference.putString(TOKEN_KEY, value)
        }

    fun getAuthorization(): String? {
        if (!isLoggedIn())
            return null
        return "JWT $token"
    }

    fun isLoggedIn(): Boolean {
        if (token == null || JWTUtils.isExpired(token)) {
            token = null
            return false
        }

        return true
    }

    fun getId(): Long {
        return if (!isLoggedIn()) -1 else JWTUtils.getIdentity(token)
    }
}
