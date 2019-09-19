package org.fossasia.openevent.general.auth

import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.utils.JWTUtils

private const val ACCESS_TOKEN = "accessToken"
private const val REFRESH_TOKEN = "refreshToken"

class AuthHolder(private val preference: Preference) {

    var accessToken: String? = null
        get() {
            return preference.getString(ACCESS_TOKEN)
        }
        set(value) {
            check(!(value != null && JWTUtils.isExpired(value))) { "Cannot set expired token" }
            field = value
            preference.putString(ACCESS_TOKEN, value)
        }
    var refreshToken: String? = null
        get() {
            return preference.getString(REFRESH_TOKEN)
        }
        set(value) {
            check(!(value != null && JWTUtils.isExpired(value))) { "Cannot set expired token" }
            field = value
            preference.putString(REFRESH_TOKEN, value)
        }

    fun getAccessAuthorization(): String? {
        if (!isLoggedIn())
            return null
        return "JWT $accessToken"
    }

    fun getRefreshAuthorization(): String? {
        if (!isLoggedIn())
            return null
        return "JWT $refreshToken"
    }

    fun getAuthorization(): String? {
        if (!isLoggedIn())
            return null
        return "JWT $accessToken"
    }

    fun isLoggedIn(): Boolean {
        if (accessToken == null || JWTUtils.isExpired(accessToken)) {
            accessToken = null
            return false
        }

        return true
    }

    fun getId(): Long {
        return if (!isLoggedIn()) -1 else JWTUtils.getIdentity(accessToken)
    }
}
