package org.fossasia.openevent.general.auth

import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Single

interface UserDao {
    @Insert(onConflict = REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM User")
    fun deleteUser()

    @Query("SELECT * from User")
    fun getUser(): Single<User>
}
