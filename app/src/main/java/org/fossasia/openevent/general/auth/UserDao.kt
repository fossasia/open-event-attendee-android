package org.fossasia.openevent.general.auth

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface UserDao {
    @Insert(onConflict = REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM User WHERE id = :id")
    fun deleteUser(id: Long)

    @Query("SELECT * from User WHERE id = :id")
    fun getUser(id: Long): Single<User>
}
