package org.fossasia.openevent.general.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
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
