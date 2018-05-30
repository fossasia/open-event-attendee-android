package org.fossasia.openevent.general.auth

import io.reactivex.Single


class AuthService(private val authApi: AuthApi,
                  private val authHolder: AuthHolder
) {


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
        authHolder.token = null
    }

    fun getProfile(id: Long = authHolder.getId()) = authApi.getProfile(id)

}

