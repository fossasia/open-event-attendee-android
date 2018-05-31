package org.fossasia.openevent.general.auth

import io.reactivex.Single
import java.util.concurrent.ConcurrentHashMap


class AuthService(private val authApi: AuthApi,
                  private val authHolder: AuthHolder
) {

    private val userMap = ConcurrentHashMap<Long, User>() // TODO: To be replaced by room

    fun login(username: String, password: String): Single<LoginResponse> {
        if (username.isEmpty() || password.isEmpty())
            throw IllegalArgumentException("Username or password cannot be empty")

        return authApi.login(Login(username, password))
                .map {
                    authHolder.token = it.accessToken
                    it
                }
    }

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun logout() {
        userMap.remove(authHolder.getId())

        authHolder.token = null
    }

    fun getProfile(id: Long = authHolder.getId()): Single<User> {
        val user = userMap[id]
        if (user != null)
            return Single.just(user)

        return authApi.getProfile(id)
                .map {
                    userMap[id] = it
                    it
                }
    }

}

