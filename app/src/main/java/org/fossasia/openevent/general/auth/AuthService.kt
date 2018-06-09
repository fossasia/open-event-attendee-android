package org.fossasia.openevent.general.auth

import io.reactivex.Completable
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

    fun logout(): Completable {
        return Completable.fromAction({
            var userFlowable = userDao.getUser(authHolder.getId())
            userFlowable.map {
                userDao.deleteUser(it)
                authHolder.token = null
            }
        })
    }


    fun getProfile(id: Long = authHolder.getId()): Single<User> {
        return userDao.getUser(id)
                .onErrorResumeNext {
                    var profileSingle = authApi.getProfile(id)
                    profileSingle.map {
                        userDao.insertUser(it)
                    }
                    return@onErrorResumeNext profileSingle
                }

    }


}

