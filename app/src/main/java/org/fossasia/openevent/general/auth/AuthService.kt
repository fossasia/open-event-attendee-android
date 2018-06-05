package org.fossasia.openevent.general.auth

import io.reactivex.Flowable
import io.reactivex.Single


class AuthService(private val authApi: AuthApi,
                  private val authHolder: AuthHolder,
                  private val userDao: UserDao
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

    fun signUp(signUp: SignUp): Single<User> {
        val email = signUp.email
        val password = signUp.password
        if (email.isNullOrEmpty() || password.isNullOrEmpty())
            throw IllegalArgumentException("Username or password cannot be empty")

        return authApi.signUp(signUp)
    }

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun logout() {
        val userFlowable = userDao.getUser(authHolder.getId())
        userFlowable.map {
            userDao.deleteUser(it)
        }
        authHolder.token = null
    }


    fun getProfile(id: Long = authHolder.getId()): Flowable<User> {
        val userFlowable = userDao.getUser(id)
        return userFlowable.switchMap {
            if (it != null)
                userFlowable
            else
                authApi.getProfile(id)
                        .map {
                            userDao.insertUser(it)
                        }
                        .toFlowable()
                        .flatMap {
                            userFlowable
                        }
        }
    }

}

