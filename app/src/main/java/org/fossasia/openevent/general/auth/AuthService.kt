package org.fossasia.openevent.general.auth

import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

class AuthService(private val authApi: AuthApi,
                  private val authHolder: AuthHolder,
                  private val userDao: UserDao
) {
    fun login(email: String, password: String): Single<LoginResponse> {
        if (email.isEmpty() || password.isEmpty())
            throw IllegalArgumentException("Username or password cannot be empty")

        return authApi.login(Login(email, password))
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

        return authApi.signUp(signUp).map {
            userDao.insertUser(it)
            it
        }
    }

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun logout(): Completable {
        return Completable.fromAction {
            authHolder.token = null
            userDao.deleteUser(authHolder.getId())
        }
    }

    fun getProfile(id: Long = authHolder.getId()): Single<User> {
        return userDao.getUser(id)
                .onErrorResumeNext {
                    Timber.d(it, "User not found in Database %d", id)
                    authApi.getProfile(id)
                            .map {
                                userDao.insertUser(it)
                                it
                            }
                }
    }
}