package org.fossasia.openevent.general

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao

@Database(entities = [Event::class, User::class], version = 1)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao
}
