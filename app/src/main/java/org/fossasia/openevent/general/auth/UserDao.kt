package org.fossasia.openevent.general.auth

import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

interface UserDao {
    @Insert
    fun insertUser(user: User)

    @Query("DELETE FROM user_table")
    fun deleteUser()
}
